import { useEffect, useState } from 'react';
import * as api from './api.js';
import { LANGS, LangContext, useI18n } from './i18n.js';

function loadUser() {
  try {
    return JSON.parse(localStorage.getItem('salon_user'));
  } catch {
    return null;
  }
}

function loadLang() {
  try {
    const saved = localStorage.getItem('salon_lang');
    if (LANGS.some((l) => l.code === saved)) return saved;
  } catch {
    // ignore
  }
  return 'en';
}

// Turn an API error into a message in the current language.
function errorText(err, t, context) {
  if (err.code === 'NETWORK') return t('serverUnreachable');
  if (err.status === 401) return t('invalidCredentials');
  if (err.status === 403) return t('forbidden');
  if (err.status === 409 && context === 'register') return t('emailTaken');
  return err.message;
}

function Message({ msg }) {
  if (!msg) return null;
  return <p className={`msg ${msg.type}`}>{msg.text}</p>;
}

function LanguageSwitch() {
  const { lang, setLang } = useI18n();
  return (
    <div className="lang" role="group" aria-label="Language">
      {LANGS.map((l) => (
        <button
          key={l.code}
          className={`lang-btn ${lang === l.code ? 'active' : ''}`}
          aria-pressed={lang === l.code}
          onClick={() => setLang(l.code)}
        >
          {l.label}
        </button>
      ))}
    </div>
  );
}

function AuthForm({ onAuth }) {
  const { t } = useI18n();
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ name: '', email: '', password: '' });
  const [msg, setMsg] = useState(null);
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setMsg(null);
    try {
      if (mode === 'register') {
        await api.register(form.name, form.email, form.password);
      }
      const user = await api.login(form.email, form.password);
      onAuth(user);
    } catch (err) {
      setMsg({ type: 'error', text: errorText(err, t, mode) });
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="card narrow">
      <h2>{mode === 'login' ? t('welcomeBack') : t('createYourAccount')}</h2>
      <form onSubmit={submit}>
        {mode === 'register' && (
          <label>
            {t('name')}
            <input value={form.name} onChange={set('name')} required />
          </label>
        )}
        <label>
          {t('email')}
          <input type="email" value={form.email} onChange={set('email')} required />
        </label>
        <label>
          {t('password')}
          <input type="password" value={form.password} onChange={set('password')} required />
        </label>
        <button disabled={busy}>{busy ? t('pleaseWait') : mode === 'login' ? t('logIn') : t('register')}</button>
      </form>
      <Message msg={msg} />
      <p className="switch">
        {mode === 'login' ? t('newHere') : t('alreadyRegistered')}{' '}
        <button
          className="link"
          onClick={() => {
            setMode(mode === 'login' ? 'register' : 'login');
            setMsg(null);
          }}
        >
          {mode === 'login' ? t('createAccount') : t('logIn')}
        </button>
      </p>
    </section>
  );
}

function pad(n) {
  return String(n).padStart(2, '0');
}

function toLocalIso(d) {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:00`;
}

function Booking({ user }) {
  const { t, label, locale } = useI18n();
  const [stylists, setStylists] = useState([]);
  const [services, setServices] = useState([]);
  const [loaded, setLoaded] = useState(false);
  const [form, setForm] = useState({ stylistId: '', serviceId: '', start: '' });
  const [availability, setAvailability] = useState(null);
  const [msg, setMsg] = useState(null);
  const [booked, setBooked] = useState([]);

  useEffect(() => {
    let cancelled = false;
    Promise.all([api.listStylists(), api.listServices()])
      .then(([st, sv]) => {
        if (cancelled) return;
        setStylists(st);
        setServices(sv);
        setForm((f) => ({
          ...f,
          stylistId: st[0] ? String(st[0].id) : '',
          serviceId: sv[0] ? String(sv[0].id) : '',
        }));
        setLoaded(true);
      })
      .catch((err) => {
        if (cancelled) return;
        setMsg({ type: 'error', text: err.code === 'NETWORK' ? t('serverUnreachable') : t('loadError') });
        setLoaded(true);
      });
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const set = (k) => (e) => {
    setForm({ ...form, [k]: e.target.value });
    setAvailability(null);
  };

  const service = services.find((s) => String(s.id) === form.serviceId);
  const stylistName = (id) => {
    const s = stylists.find((x) => x.id === id);
    return s ? label(s) : `#${id}`;
  };
  const serviceName = (id) => {
    const s = services.find((x) => x.id === id);
    return s ? label(s) : `#${id}`;
  };

  function range() {
    const start = new Date(form.start);
    const end = new Date(start.getTime() + (service ? service.durationMinutes : 60) * 60000);
    return { startTime: toLocalIso(start), endTime: toLocalIso(end) };
  }

  async function check(e) {
    e.preventDefault();
    setMsg(null);
    try {
      const { startTime, endTime } = range();
      const res = await api.checkAvailability(form.stylistId, startTime, endTime);
      setAvailability(res.available);
    } catch (err) {
      setMsg({ type: 'error', text: errorText(err, t) });
    }
  }

  async function book() {
    setMsg(null);
    try {
      const { startTime, endTime } = range();
      const appt = await api.bookAppointment({
        customerId: user.id,
        stylistId: Number(form.stylistId),
        serviceId: Number(form.serviceId),
        startTime,
        endTime,
      });
      setBooked([appt, ...booked]);
      setAvailability(null);
      setMsg({ type: 'ok', text: t('booked', { id: appt.id }) });
    } catch (err) {
      setMsg({ type: 'error', text: err.status === 409 ? t('slotUnavailable') : errorText(err, t) });
    }
  }

  async function cancel(id) {
    setMsg(null);
    try {
      const res = await api.cancelAppointment(id, user.id);
      setBooked(booked.map((a) => (a.id === id ? res : a)));
      setMsg({ type: 'ok', text: t('cancelledMsg', { id }) });
    } catch (err) {
      setMsg({ type: 'error', text: errorText(err, t) });
    }
  }

  return (
    <>
      <section className="card">
        <h2>{t('bookTitle')}</h2>
        {!loaded ? (
          <p className="muted">{t('loading')}</p>
        ) : (
          <form onSubmit={check}>
            <div className="row">
              <label>
                {t('stylist')}
                <select value={form.stylistId} onChange={set('stylistId')} required>
                  {stylists.map((s) => (
                    <option key={s.id} value={s.id}>
                      {label(s)}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                {t('service')}
                <select value={form.serviceId} onChange={set('serviceId')} required>
                  {services.map((s) => (
                    <option key={s.id} value={s.id}>
                      {label(s)} · {s.durationMinutes} {t('minutes')}
                    </option>
                  ))}
                </select>
              </label>
            </div>
            <label>
              {t('start')}
              <input type="datetime-local" value={form.start} onChange={set('start')} required />
            </label>
            <button type="submit" disabled={!stylists.length || !services.length}>
              {t('checkAvailability')}
            </button>
          </form>
        )}
        {availability === true && (
          <div className="available">
            <p className="msg ok">{t('slotFree')}</p>
            <button onClick={book}>{t('bookIt')}</button>
          </div>
        )}
        {availability === false && <p className="msg error">{t('slotUnavailable')}</p>}
        <Message msg={msg} />
      </section>

      <section className="card">
        <h2>{t('yourBookings')}</h2>
        {booked.length === 0 ? (
          <p className="muted">{t('nothingBooked')}</p>
        ) : (
          <ul className="list">
            {booked.map((a) => (
              <li key={a.id}>
                <div>
                  <strong>#{a.id}</strong> · {stylistName(a.stylistId)} · {serviceName(a.serviceId)}
                  <br />
                  <span className="muted">
                    {new Date(a.startTime).toLocaleString(locale)} →{' '}
                    {new Date(a.endTime).toLocaleTimeString(locale, { hour: '2-digit', minute: '2-digit' })}
                  </span>
                </div>
                {a.status === 'CANCELLED' ? (
                  <span className="badge">{t('cancelledBadge')}</span>
                ) : (
                  <button className="secondary" onClick={() => cancel(a.id)}>
                    {t('cancel')}
                  </button>
                )}
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  );
}

function Shell() {
  const { t } = useI18n();
  const [user, setUser] = useState(loadUser);

  function onAuth(u) {
    localStorage.setItem('salon_user', JSON.stringify(u));
    setUser(u);
  }
  function logout() {
    localStorage.removeItem('salon_user');
    setUser(null);
  }

  return (
    <div className="app">
      <header>
        <h1>SalonBooking</h1>
        <div className="who">
          <LanguageSwitch />
          {user && (
            <>
              <span>{user.name}</span>
              <button className="link" onClick={logout}>
                {t('logOut')}
              </button>
            </>
          )}
        </div>
      </header>
      <main>{user ? <Booking user={user} /> : <AuthForm onAuth={onAuth} />}</main>
      <footer>{t('footer')}</footer>
    </div>
  );
}

export default function App() {
  const [lang, setLangState] = useState(loadLang);

  useEffect(() => {
    document.documentElement.lang = lang;
  }, [lang]);

  function setLang(code) {
    try {
      localStorage.setItem('salon_lang', code);
    } catch {
      // ignore
    }
    setLangState(code);
  }

  return (
    <LangContext.Provider value={{ lang, setLang }}>
      <Shell />
    </LangContext.Provider>
  );
}

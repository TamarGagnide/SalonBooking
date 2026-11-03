import { useState } from 'react';
import * as api from './api.js';

function loadUser() {
  try {
    return JSON.parse(localStorage.getItem('salon_user'));
  } catch {
    return null;
  }
}

function Message({ msg }) {
  if (!msg) return null;
  return <p className={`msg ${msg.type}`}>{msg.text}</p>;
}

function AuthForm({ onAuth }) {
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
      setMsg({ type: 'error', text: err.message });
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="card narrow">
      <h2>{mode === 'login' ? 'Welcome back' : 'Create your account'}</h2>
      <form onSubmit={submit}>
        {mode === 'register' && (
          <label>
            Name
            <input value={form.name} onChange={set('name')} required />
          </label>
        )}
        <label>
          Email
          <input type="email" value={form.email} onChange={set('email')} required />
        </label>
        <label>
          Password
          <input type="password" value={form.password} onChange={set('password')} required />
        </label>
        <button disabled={busy}>{busy ? 'Please wait…' : mode === 'login' ? 'Log in' : 'Register'}</button>
      </form>
      <Message msg={msg} />
      <p className="switch">
        {mode === 'login' ? 'New here?' : 'Already registered?'}{' '}
        <button
          className="link"
          onClick={() => {
            setMode(mode === 'login' ? 'register' : 'login');
            setMsg(null);
          }}
        >
          {mode === 'login' ? 'Create an account' : 'Log in'}
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
  const [form, setForm] = useState({ stylistId: '1', serviceId: '1', start: '', minutes: '60' });
  const [availability, setAvailability] = useState(null);
  const [msg, setMsg] = useState(null);
  const [booked, setBooked] = useState([]);
  const set = (k) => (e) => {
    setForm({ ...form, [k]: e.target.value });
    setAvailability(null);
  };

  function range() {
    const start = new Date(form.start);
    const end = new Date(start.getTime() + Number(form.minutes) * 60000);
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
      setMsg({ type: 'error', text: err.message });
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
      setMsg({ type: 'ok', text: `Booked! Appointment #${appt.id} is confirmed.` });
    } catch (err) {
      setMsg({ type: 'error', text: err.message });
    }
  }

  async function cancel(id) {
    setMsg(null);
    try {
      const res = await api.cancelAppointment(id, user.id);
      setBooked(booked.map((a) => (a.id === id ? res : a)));
      setMsg({ type: 'ok', text: `Appointment #${id} cancelled.` });
    } catch (err) {
      setMsg({ type: 'error', text: err.message });
    }
  }

  return (
    <>
      <section className="card">
        <h2>Book an appointment</h2>
        <form onSubmit={check}>
          <div className="row">
            <label>
              Stylist ID
              <input type="number" min="1" value={form.stylistId} onChange={set('stylistId')} required />
            </label>
            <label>
              Service ID
              <input type="number" min="1" value={form.serviceId} onChange={set('serviceId')} required />
            </label>
          </div>
          <div className="row">
            <label>
              Start
              <input type="datetime-local" value={form.start} onChange={set('start')} required />
            </label>
            <label>
              Duration
              <select value={form.minutes} onChange={set('minutes')}>
                <option value="30">30 min</option>
                <option value="60">60 min</option>
                <option value="90">90 min</option>
                <option value="120">120 min</option>
              </select>
            </label>
          </div>
          <button type="submit">Check availability</button>
        </form>
        {availability === true && (
          <div className="available">
            <p className="msg ok">This slot is free.</p>
            <button onClick={book}>Book it</button>
          </div>
        )}
        {availability === false && <p className="msg error">That slot is taken or outside working hours.</p>}
        <Message msg={msg} />
      </section>

      <section className="card">
        <h2>Your bookings this session</h2>
        {booked.length === 0 ? (
          <p className="muted">Nothing booked yet.</p>
        ) : (
          <ul className="list">
            {booked.map((a) => (
              <li key={a.id}>
                <div>
                  <strong>#{a.id}</strong> · stylist {a.stylistId} · service {a.serviceId}
                  <br />
                  <span className="muted">
                    {new Date(a.startTime).toLocaleString()} →{' '}
                    {new Date(a.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </span>
                </div>
                {a.status === 'CANCELLED' ? (
                  <span className="badge">Cancelled</span>
                ) : (
                  <button className="secondary" onClick={() => cancel(a.id)}>
                    Cancel
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

export default function App() {
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
        {user && (
          <div className="who">
            <span>{user.name}</span>
            <button className="link" onClick={logout}>
              Log out
            </button>
          </div>
        )}
      </header>
      <main>{user ? <Booking user={user} /> : <AuthForm onAuth={onAuth} />}</main>
      <footer>Minimal MVP · more features coming</footer>
    </div>
  );
}

import { createContext, useContext } from 'react';

export const LANGS = [
  { code: 'en', label: 'EN' },
  { code: 'ka', label: 'ქარ' },
];

export const messages = {
  en: {
    welcomeBack: 'Welcome back',
    createYourAccount: 'Create your account',
    name: 'Name',
    email: 'Email',
    password: 'Password',
    logIn: 'Log in',
    register: 'Register',
    pleaseWait: 'Please wait…',
    newHere: 'New here?',
    createAccount: 'Create an account',
    alreadyRegistered: 'Already registered?',
    logOut: 'Log out',
    bookTitle: 'Book an appointment',
    stylist: 'Stylist',
    service: 'Service',
    start: 'Start',
    duration: 'Duration',
    minutes: 'min',
    checkAvailability: 'Check availability',
    slotFree: 'This slot is free.',
    bookIt: 'Book it',
    slotUnavailable: 'That slot is taken or outside working hours.',
    booked: 'Booked! Appointment #{id} is confirmed.',
    cancelledMsg: 'Appointment #{id} cancelled.',
    yourBookings: 'Your bookings this session',
    nothingBooked: 'Nothing booked yet.',
    cancel: 'Cancel',
    cancelledBadge: 'Cancelled',
    footer: 'Minimal MVP · more features coming',
    loading: 'Loading…',
    loadError: 'Could not load stylists and services.',
    serverUnreachable: 'Cannot reach the server. If it was idle, give it a minute to wake up and try again.',
    invalidCredentials: 'Wrong email or password.',
    emailTaken: 'This email is already registered.',
    forbidden: 'You are not allowed to do that.',
  },
  ka: {
    welcomeBack: 'კეთილი იყოს თქვენი დაბრუნება',
    createYourAccount: 'ანგარიშის შექმნა',
    name: 'სახელი',
    email: 'ელფოსტა',
    password: 'პაროლი',
    logIn: 'შესვლა',
    register: 'რეგისტრაცია',
    pleaseWait: 'მოიცადეთ…',
    newHere: 'პირველად ხართ?',
    createAccount: 'შექმენით ანგარიში',
    alreadyRegistered: 'უკვე გაქვთ ანგარიში?',
    logOut: 'გასვლა',
    bookTitle: 'ვიზიტის დაჯავშნა',
    stylist: 'სტილისტი',
    service: 'მომსახურება',
    start: 'დაწყება',
    duration: 'ხანგრძლივობა',
    minutes: 'წთ',
    checkAvailability: 'თავისუფალი დროის შემოწმება',
    slotFree: 'ეს დრო თავისუფალია.',
    bookIt: 'დაჯავშნა',
    slotUnavailable: 'ეს დრო დაკავებულია ან სამუშაო საათების მიღმაა.',
    booked: 'დაჯავშნილია! ვიზიტი #{id} დადასტურდა.',
    cancelledMsg: 'ვიზიტი #{id} გაუქმდა.',
    yourBookings: 'თქვენი ჯავშნები ამ სესიაში',
    nothingBooked: 'ჯერ არაფერია დაჯავშნილი.',
    cancel: 'გაუქმება',
    cancelledBadge: 'გაუქმებულია',
    footer: 'მინიმალური MVP · მალე მეტი ფუნქცია',
    loading: 'იტვირთება…',
    loadError: 'სტილისტებისა და მომსახურებების ჩატვირთვა ვერ მოხერხდა.',
    serverUnreachable: 'სერვერთან დაკავშირება ვერ მოხერხდა. თუ ის უქმად იყო, მოიცადეთ ერთი წუთი და სცადეთ თავიდან.',
    invalidCredentials: 'ელფოსტა ან პაროლი არასწორია.',
    emailTaken: 'ეს ელფოსტა უკვე რეგისტრირებულია.',
    forbidden: 'ამ მოქმედების უფლება არ გაქვთ.',
  },
};

export const LangContext = createContext({ lang: 'en', setLang: () => {} });

export function useI18n() {
  const { lang, setLang } = useContext(LangContext);
  const dict = messages[lang] || messages.en;
  const t = (key, vars = {}) =>
    Object.entries(vars).reduce((s, [k, v]) => s.replace(`{${k}}`, v), dict[key] ?? messages.en[key] ?? key);
  // pick the Georgian name for items that have one, otherwise the default name
  const label = (item) => (lang === 'ka' && item.nameKa ? item.nameKa : item.name);
  const locale = lang === 'ka' ? 'ka-GE' : 'en-GB';
  return { lang, setLang, t, label, locale };
}

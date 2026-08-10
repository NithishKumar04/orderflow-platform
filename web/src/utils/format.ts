const currency = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
});

const date = new Intl.DateTimeFormat("en-US", {
  day: "numeric",
  month: "short",
  year: "numeric",
});

const time = new Intl.DateTimeFormat("en-US", {
  hour: "numeric",
  minute: "2-digit",
  second: "2-digit",
});

export function formatCurrency(value: number) {
  return currency.format(value);
}

export function formatDate(value: string) {
  return date.format(new Date(value));
}

export function formatTime(value: string) {
  return time.format(new Date(value));
}

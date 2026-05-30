const formatter = new Intl.NumberFormat("fr-MA", {
  style: "currency",
  currency: "MAD",
  minimumFractionDigits: 2,
});

export const formatCurrency = (value) => {
  const amount = Number(value);
  if (Number.isNaN(amount)) {
    return "0.00 DH";
  }

  const formatted = formatter.format(amount);
  return formatted.replace("MAD", "DH").trim();
};

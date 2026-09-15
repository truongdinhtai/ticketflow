import client from './client';

export async function createBooking(payload) {
  const { data } = await client.post('/api/bookings', payload);
  return data;
}

// Booking Service exposes a paged list; we fetch a page and filter to the
// logged-in user's email client-side (a production API would filter server-side
// or derive the user from the JWT the gateway forwards).
export async function fetchMyBookings(email, { page = 0, size = 200 } = {}) {
  const { data } = await client.get('/api/bookings', { params: { page, size } });
  const all = data.content || [];
  return all.filter((b) => b.customerEmail === email);
}

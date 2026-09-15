import client from './client';

export async function fetchEvents({ page = 0, size = 50 } = {}) {
  const { data } = await client.get('/api/events', { params: { page, size } });
  return data; // Spring Page: { content, totalElements, ... }
}

export async function fetchEvent(id) {
  const { data } = await client.get(`/api/events/${id}`);
  return data;
}

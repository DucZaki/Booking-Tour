// Minimal client helper to call backend /api/chat
async function sendMessage(text) {
  const res = await fetch('/api/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ message: text })
  });
  if (!res.ok) throw new Error('Network error: ' + res.status);
  const j = await res.json();
  return j.reply;
}

// Example usage (you can wire this to your UI):
// sendMessage('Hello').then(r => console.log(r)).catch(e => console.error(e));

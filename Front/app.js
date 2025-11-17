/* Front -> Backend integration (simple fetch to Spring Boot API)
   Base API: http://localhost:8081/api/users
   The backend expects fields: nom, email, role, password
   The front keeps firstName/lastName inputs; we send nom = firstName + ' ' + lastName
*/

const API_BASE = 'http://localhost:8081/api/users';

// Elements
const usersTableBody = document.querySelector('#usersTable tbody');
const btnNew = document.getElementById('btnNew');
const formPanel = document.getElementById('formPanel');
const userForm = document.getElementById('userForm');
const btnSave = document.getElementById('btnSave');
const btnEdit = document.getElementById('btnEdit');
const btnCancel = document.getElementById('btnCancel');

let users = [];
let currentUserId = null;
let isCreating = false; 

function escapeHtml(s) {
  if (!s) return '';
  return String(s).replace(/[&<>"']/g, function (c) { return { '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' }[c]; });
}

async function loadUsers() {
  try {
    const res = await fetch(API_BASE);
    if (!res.ok) throw new Error('Failed to load users');
    users = await res.json();
    renderTable();
  } catch (e) {
    console.error(e);
    users = [];
    renderTable();
  }
}

function renderTable() {
  usersTableBody.innerHTML = '';
  users.forEach(u => {
    const tr = document.createElement('tr');
    tr.dataset.id = u.id;
    // u.nom is a single field in backend; try to split for front display
    const nameParts = (u.nom || '').split(' ');
    const first = escapeHtml(nameParts.shift() || '');
    const last = escapeHtml(nameParts.join(' ') || '');
    tr.innerHTML = `
      <td>${u.id}</td>
      <td>${first}</td>
      <td>${last}</td>
      <td>${escapeHtml(u.email)}</td>
      <td>${escapeHtml(u.role || '')}</td>
    `;
    tr.addEventListener('click', () => onRowClick(u.id));
    usersTableBody.appendChild(tr);
  });
}

async function fetchUser(id) {
  const res = await fetch(`${API_BASE}/${id}`);
  if (res.status === 404) return null;
  if (!res.ok) throw new Error('Failed to fetch user');
  return res.json();
}

function onRowClick(id) {
  currentUserId = id;
  isCreating = false;
  fetchUser(id).then(u => {
    if (!u) return;
    showForm(u, { editable: false });
  }).catch(err => console.error(err));
}

function showForm(data = {}, options = { editable: true }) {
  formPanel.classList.remove('hidden');
  userForm.id.value = data.id || '';
  // split backend nom into first / last
  const parts = (data.nom || '').split(' ');
  userForm.firstName.value = parts.shift() || '';
  userForm.lastName.value = parts.join(' ') || '';
  userForm.email.value = data.email || '';
  userForm.role.value = data.role || '';
  userForm.password.value = '';
  setFormEditable(!!options.editable);
}

function hideForm() {
  formPanel.classList.add('hidden');
  currentUserId = null;
  isCreating = false;
  userForm.reset();
}

function setFormEditable(flag) {
  const inputs = userForm.querySelectorAll('input');
  inputs.forEach(i => { if (i.type !== 'hidden') i.disabled = !flag; });
  btnSave.style.display = flag ? '' : 'none';
  btnEdit.style.display = flag ? 'none' : '';
}

btnNew.addEventListener('click', () => {
  isCreating = true;
  currentUserId = null;
  userForm.reset();
  showForm({}, { editable: true });
});

btnEdit.addEventListener('click', () => setFormEditable(true));
btnCancel.addEventListener('click', () => hideForm());

userForm.addEventListener('submit', async function (e) {
  e.preventDefault();
  const idVal = userForm.id.value ? Number(userForm.id.value) : null;
  const nom = `${userForm.firstName.value.trim()} ${userForm.lastName.value.trim()}`.trim();
  const payload = {
    nom,
    email: userForm.email.value.trim(),
    role: userForm.role.value.trim(),
    password: userForm.password.value // assume backend handles hashing
  };

  try {
    if (isCreating) {
      const res = await fetch(API_BASE, {
        method: 'POST',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify(payload)
      });
      if (!res.ok) {
        const t = await res.text();
        throw new Error('Create failed: ' + t);
      }
    } else {
      const res = await fetch(`${API_BASE}/${idVal}`, {
        method: 'PUT',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify(payload)
      });
      if (!res.ok) {
        const t = await res.text();
        throw new Error('Update failed: ' + t);
      }
    }
    await loadUsers();
    hideForm();
  } catch (err) {
    console.error(err);
    alert(err.message);
  }
});

// delete helper
async function deleteUser(id) {
  const res = await fetch(`${API_BASE}/${id}`, { method: 'DELETE' });
  if (res.status === 204) return true;
  throw new Error('Delete failed');
}

// Add context menu for delete (simple): right-click row to delete (confirm)
usersTableBody.addEventListener('contextmenu', async (ev) => {
  ev.preventDefault();
  const tr = ev.target.closest('tr');
  if (!tr) return;
  const id = tr.dataset.id;
  if (!id) return;
  if (!confirm('Supprimer cet utilisateur ?')) return;
  try {
    await deleteUser(id);
    await loadUsers();
  } catch (e) {
    console.error(e);
    alert('Suppression échouée');
  }
});

// init
(function init() {
  loadUsers();
})();

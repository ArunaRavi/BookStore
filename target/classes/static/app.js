const state = {
  categories: [],
  products: [],
  authors: [],
  token: sessionStorage.getItem('bookworm-token'),
  user: JSON.parse(sessionStorage.getItem('bookworm-user') || 'null'),
  basket: null,
  selectedCategory: 'all',
  currentView: 'catalog',
  search: '',
  sortBy: 'relevance',
  format: 'all',
  language: 'all',
  minPrice: '',
  maxPrice: '',
  toastTimer: null
};

const elements = {
  categoryList: document.querySelector('#category-list'),
  searchForm: document.querySelector('#search-form'),
  searchInput: document.querySelector('#search-input'),
  sortFilter: document.querySelector('#sort-filter'),
  formatFilter: document.querySelector('#format-filter'),
  languageFilter: document.querySelector('#language-filter'),
  priceFilter: document.querySelector('#price-filter'),
  statusMessage: document.querySelector('#status-message'),
  catalogView: document.querySelector('#catalog-view'),
  resultsView: document.querySelector('#results-view'),
  writersView: document.querySelector('#writers-view'),
  ordersView: document.querySelector('#orders-view'),
  loginOverlay: document.querySelector('#login-overlay'),
  productOverlay: document.querySelector('#product-overlay'),
  productDetail: document.querySelector('#product-detail-content'),
  basketDrawer: document.querySelector('#basket-drawer'),
  basketItems: document.querySelector('#basket-items'),
  basketSummary: document.querySelector('#basket-summary'),
  toast: document.querySelector('#toast')
};

const wishlistKey = 'bookworm-wishlist';
const savedIds = () => new Set(JSON.parse(localStorage.getItem(wishlistKey) || '[]'));
const escapeHtml = (value = '') => String(value).replace(/[&<>"']/g, (char) => ({
  '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
}[char]));
const money = (amount, symbol = 'INR') => `${escapeHtml(symbol)} ${Number(amount || 0).toLocaleString('en-IN', { maximumFractionDigits: 2 })}`;

async function api(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (state.token) headers.Authorization = `Bearer ${state.token}`;
  const response = await fetch(path, { ...options, headers });
  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try {
      const body = await response.json();
      message = body.message || body.error || message;
    } catch { /* The response may not contain JSON. */ }
    const error = new Error(message);
    error.status = response.status;
    throw error;
  }
  if (response.status === 204) return null;
  return response.json();
}

function showToast(message) {
  elements.toast.textContent = message;
  elements.toast.classList.add('is-visible');
  clearTimeout(state.toastTimer);
  state.toastTimer = setTimeout(() => elements.toast.classList.remove('is-visible'), 2600);
}

function setStatus(message = '') {
  elements.statusMessage.textContent = message;
}

function setView(name) {
  state.currentView = name;
  const contentView = name === 'wishlist' ? 'results' : name;
  elements.catalogView.classList.toggle('is-hidden', contentView !== 'catalog');
  elements.resultsView.classList.toggle('is-hidden', contentView !== 'results');
  elements.writersView.classList.toggle('is-hidden', contentView !== 'writers');
  elements.ordersView.classList.toggle('is-hidden', contentView !== 'orders');
  document.querySelectorAll('.nav-link').forEach((button) => {
    button.classList.toggle('is-active', button.dataset.view === name);
  });
}

function buildQuery({ category = state.selectedCategory, search = state.search, sortBy = state.sortBy } = {}) {
  const params = new URLSearchParams({ page: '1', limit: '100', sortBy });
  if (search.trim()) params.set('search', search.trim());
  if (category && category !== 'all') params.set('categoryId', category);
  if (state.minPrice) params.set('minPrice', state.minPrice);
  if (state.maxPrice) params.set('maxPrice', state.maxPrice);
  if (state.language !== 'all') params.set('language', state.language);
  return params.toString();
}

async function loadCategories() {
  state.categories = await api('/categories');
  elements.categoryList.innerHTML = '<button class="category-button is-active" data-category="all">All books</button>' +
    state.categories.map((category) => `<button class="category-button" data-category="${escapeHtml(category.categoryId)}">${escapeHtml(category.name)}</button>`).join('');
}

function renderBook(product) {
  const isSaved = savedIds().has(product.productId);
  const author = product.brand?.name || 'Unknown author';
  return `<article class="book-card">
    <button class="cover-button" data-product="${escapeHtml(product.productId)}" aria-label="View ${escapeHtml(product.title)} details">
      <img class="book-cover" src="${escapeHtml(product.coverImageUrl || '')}" alt="Cover of ${escapeHtml(product.title)}" loading="lazy">
    </button>
    <div class="book-info">
      <h3 class="book-title">${escapeHtml(product.title)}</h3>
      <p class="book-author">by <button data-author="${escapeHtml(product.brand?.brandId || '')}">${escapeHtml(author)}</button></p>
      <p class="book-description">${escapeHtml(product.synopsis || '')}</p>
      <p class="book-meta">${escapeHtml(product.format || 'Book')} · ${escapeHtml(product.category || '')}</p>
      <div class="book-buy-row">
        <span class="book-price">${money(product.price, product.currencySymbol)}</span>
        <span class="book-actions">
          <button class="mini-button ${isSaved ? 'is-saved' : ''}" data-wishlist="${escapeHtml(product.productId)}" aria-label="${isSaved ? 'Remove from' : 'Add to'} wishlist" title="${isSaved ? 'Remove from wishlist' : 'Save to wishlist'}">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M20.8 8.8c0 5.2-8.8 10-8.8 10s-8.8-4.8-8.8-10A4.8 4.8 0 0 1 12 6.1a4.8 4.8 0 0 1 8.8 2.7Z"/></svg>
          </button>
          <button class="mini-button" data-add="${escapeHtml(product.productId)}" aria-label="Add ${escapeHtml(product.title)} to basket" title="Add to basket">＋</button>
        </span>
      </div>
    </div>
  </article>`;
}

function renderShelves(products) {
  const groups = [products.slice(0, 3), products.slice(3, 6), products.slice(6, 9)];
  const targets = ['#recommended-books', '#bestseller-books', '#launch-books'];
  const shelves = ['#recommended-shelf', '#bestsellers-shelf', '#launches-shelf'];
  groups.forEach((group, index) => {
    document.querySelector(targets[index]).innerHTML = group.map(renderBook).join('');
    document.querySelector(shelves[index]).classList.toggle('is-hidden', group.length === 0);
  });
}

function renderResults(products, title) {
  document.querySelector('#results-title').textContent = title;
  document.querySelector('#results-count').textContent = `${products.length} ${products.length === 1 ? 'book' : 'books'}`;
  document.querySelector('#results-books').innerHTML = products.map(renderBook).join('');
  document.querySelector('#empty-state').classList.toggle('is-hidden', products.length > 0);
}

async function loadProducts({ initial = false } = {}) {
  setStatus(initial ? 'Finding your next read...' : 'Updating the shelf...');
  try {
    const result = await api(`/products?${buildQuery({ sortBy: initial ? 'oldest' : state.sortBy })}`);
    state.products = result.items || [];
    const products = state.products.filter((product) => {
      const matchesFormat = state.format === 'all' || product.format?.toLowerCase() === state.format.toLowerCase();
      return matchesFormat;
    });
    setStatus('');
    const hasFilters = state.search.trim() || state.selectedCategory !== 'all' || state.format !== 'all' || state.language !== 'all' || state.minPrice || state.maxPrice;
    if (hasFilters || state.currentView === 'results') {
      setView('results');
      const categoryName = state.categories.find((category) => category.categoryId === state.selectedCategory)?.name;
      renderResults(products, state.search ? `Results for “${state.search}”` : categoryName || 'Browse all books');
    } else {
      setView('catalog');
      renderShelves(products);
    }
  } catch (error) {
    setStatus(`The catalogue could not be loaded. ${error.message}`);
  }
}

async function openProduct(productId) {
  try {
    const detail = await api(`/products/${encodeURIComponent(productId)}`);
    const author = detail.brand?.name || 'Unknown author';
    elements.productDetail.innerHTML = `<div class="product-detail">
      <img class="detail-cover" src="${escapeHtml(detail.coverImageUrl || '')}" alt="Cover of ${escapeHtml(detail.title)}">
      <div class="detail-copy">
        <p class="eyebrow">${escapeHtml(detail.category || 'Book')}</p>
        <h2 id="product-title">${escapeHtml(detail.title)}</h2>
        <p class="detail-author">by ${escapeHtml(author)}</p>
        <p>${escapeHtml(detail.fullDescription || detail.synopsis || '')}</p>
        <div class="detail-facts">
          <span>Format<strong>${escapeHtml(detail.format || 'Book')}</strong></span>
          <span>Language<strong>${escapeHtml(detail.language || 'English')}</strong></span>
          <span>Publisher<strong>${escapeHtml(detail.publisher || 'Book Worm')}</strong></span>
          <span>Delivery<strong>${escapeHtml(detail.tentativeDeliveryDate || 'Calculated at checkout')}</strong></span>
        </div>
        <p class="detail-price">${money(detail.price, detail.currencySymbol)}</p>
        <button class="solid-button" data-add="${escapeHtml(detail.productId)}">Add to basket</button>
      </div>
    </div>`;
    elements.productOverlay.classList.remove('is-hidden');
  } catch (error) {
    showToast(error.message);
  }
}

function updateAccount() {
  const name = state.user?.name;
  document.querySelector('#account-label').textContent = name ? name.split(' ')[0] : 'Sign in';
  document.querySelector('#account-avatar').textContent = name ? name.charAt(0).toUpperCase() : '?';
  document.querySelector('#account-button').setAttribute('aria-label', name ? `Signed in as ${name}; sign out` : 'Sign in');
}

function openLogin() {
  elements.loginOverlay.classList.remove('is-hidden');
  document.querySelector('#login-error').textContent = '';
  document.querySelector('#login-form input[name="email"]').focus();
}

async function signIn(event) {
  event.preventDefault();
  const form = new FormData(event.currentTarget);
  const button = event.currentTarget.querySelector('button[type="submit"]');
  const errorNode = document.querySelector('#login-error');
  button.disabled = true;
  button.textContent = 'Signing in...';
  errorNode.textContent = '';
  try {
    const result = await api('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email: form.get('email'), password: form.get('password') })
    });
    state.token = result.accessToken;
    state.user = result.user;
    sessionStorage.setItem('bookworm-token', state.token);
    sessionStorage.setItem('bookworm-user', JSON.stringify(state.user));
    elements.loginOverlay.classList.add('is-hidden');
    updateAccount();
    await loadBasket();
    showToast(`Welcome back, ${state.user.name.split(' ')[0]}.`);
  } catch (error) {
    errorNode.textContent = error.status === 401 ? 'Email or password is incorrect.' : error.message;
  } finally {
    button.disabled = false;
    button.textContent = 'Sign in';
  }
}

async function loadBasket() {
  if (!state.token) return;
  try {
    state.basket = await api('/basket');
    document.querySelector('#cart-count').textContent = state.basket.totalItemCount || 0;
    renderBasket();
  } catch (error) {
    if (error.status === 401) signOut(false);
    else showToast(error.message);
  }
}

async function addToBasket(productId) {
  if (!state.token) {
    openLogin();
    showToast('Sign in to add a book to your basket.');
    return;
  }
  try {
    state.basket = await api('/basket/items', {
      method: 'POST',
      body: JSON.stringify({ productId, quantity: 1 })
    });
    document.querySelector('#cart-count').textContent = state.basket.totalItemCount || 0;
    renderBasket();
    showToast('Added to your basket.');
  } catch (error) {
    showToast(error.message);
  }
}

function renderBasket() {
  const items = state.basket?.items || [];
  if (!items.length) {
    elements.basketItems.innerHTML = '<p class="drawer-empty">Your basket is waiting for a story.<br>Browse the shelves and add a book.</p>';
  } else {
    elements.basketItems.innerHTML = items.map((item) => `<div class="basket-item">
      <img src="${escapeHtml(item.product.coverImageUrl || '')}" alt="" loading="lazy">
      <div><strong>${escapeHtml(item.product.title)}</strong><span>Qty ${Number(item.quantity)} · ${money(item.unitPrice, state.basket.currencySymbol)}</span></div>
      <button data-remove="${escapeHtml(item.itemId)}" aria-label="Remove ${escapeHtml(item.product.title)}">Remove</button>
    </div>`).join('');
  }
  if (state.basket) {
    elements.basketSummary.innerHTML = `<div class="summary-line"><span>Subtotal</span><span>${money(state.basket.subtotal, state.basket.currencySymbol)}</span></div>
      <div class="summary-line"><span>Delivery</span><span>${money(state.basket.deliveryFee, state.basket.currencySymbol)}</span></div>
      <div class="summary-line total"><span>Total</span><span>${money(state.basket.totalAmount, state.basket.currencySymbol)}</span></div>`;
  }
}

function openBasket() {
  if (!state.token) {
    openLogin();
    return;
  }
  elements.basketDrawer.classList.remove('is-hidden');
  elements.basketDrawer.setAttribute('aria-hidden', 'false');
  document.querySelector('#drawer-scrim').classList.remove('is-hidden');
}

function closeBasket() {
  elements.basketDrawer.classList.add('is-hidden');
  elements.basketDrawer.setAttribute('aria-hidden', 'true');
  document.querySelector('#drawer-scrim').classList.add('is-hidden');
}

async function removeBasketItem(itemId) {
  try {
    state.basket = await api(`/basket/items/${encodeURIComponent(itemId)}`, { method: 'DELETE' });
    document.querySelector('#cart-count').textContent = state.basket.totalItemCount || 0;
    renderBasket();
  } catch (error) {
    showToast(error.message);
  }
}

function toggleWishlist(productId) {
  const wishlist = savedIds();
  if (wishlist.has(productId)) wishlist.delete(productId);
  else wishlist.add(productId);
  localStorage.setItem(wishlistKey, JSON.stringify([...wishlist]));
  if (state.currentView === 'wishlist') showWishlist();
  else loadProducts();
  showToast(wishlist.has(productId) ? 'Saved to your wishlist.' : 'Removed from your wishlist.');
}

function showWishlist() {
  const wishlist = savedIds();
  const products = state.products.filter((product) => wishlist.has(product.productId));
  setView('wishlist');
  renderResults(products, 'My Wishlist');
}

async function showWriters() {
  setView('writers');
  document.querySelector('#writer-grid').innerHTML = '<p class="empty-state">Loading writers...</p>';
  try {
    state.authors = await api('/brands');
    document.querySelector('#writer-grid').innerHTML = state.authors.map((author) => `<button class="writer-card" data-author="${escapeHtml(author.brandId)}">
      <img class="writer-avatar" src="${escapeHtml(author.avatarUrl || '')}" alt="" loading="lazy">
      <span><strong>${escapeHtml(author.name)}</strong><span>${escapeHtml(author.type || 'Writer')}</span></span>
    </button>`).join('') || '<p class="empty-state">No writers found.</p>';
  } catch (error) {
    document.querySelector('#writer-grid').innerHTML = `<p class="empty-state">${escapeHtml(error.message)}</p>`;
  }
}

async function showAuthor(brandId) {
  if (!brandId) return;
  const author = state.authors.find((item) => item.brandId === brandId) || state.products.find((item) => item.brand?.brandId === brandId)?.brand;
  try {
    const result = await api(`/brands/${encodeURIComponent(brandId)}/products?page=1&limit=100`);
    setView('results');
    renderResults(result.items || [], author ? `Books by ${author.name}` : 'Books by this writer');
  } catch (error) {
    showToast(error.message);
  }
}

async function showOrders() {
  if (!state.token) {
    openLogin();
    return;
  }
  setView('orders');
  document.querySelector('#order-list').innerHTML = '<p class="empty-state">Loading orders...</p>';
  try {
    const result = await api('/orders/history?page=1&limit=20');
    const orders = result.orders || [];
    document.querySelector('#order-list').innerHTML = orders.length ? orders.map((order) => `<article class="order-row">
      <div><strong>${escapeHtml(order.items?.map((item) => item.title).join(', ') || 'Book Worm order')}</strong><span>${escapeHtml(order.orderTimestamp || '')} · ${escapeHtml(order.status || '')}</span></div>
      <span class="order-total">${money(order.totalPaid)}</span>
    </article>`).join('') : '<p class="empty-state">No orders yet. Your next favorite is on the shelf.</p>';
  } catch (error) {
    document.querySelector('#order-list').innerHTML = `<p class="empty-state">${escapeHtml(error.message)}</p>`;
  }
}

function signOut(showMessage = true) {
  if (state.token) api('/auth/logout', { method: 'POST' }).catch(() => {});
  state.token = null;
  state.user = null;
  state.basket = null;
  sessionStorage.removeItem('bookworm-token');
  sessionStorage.removeItem('bookworm-user');
  document.querySelector('#cart-count').textContent = '0';
  updateAccount();
  closeBasket();
  if (showMessage) showToast('You have signed out.');
}

function applyPriceFilter() {
  const [min, max] = elements.priceFilter.value === 'all' ? ['', ''] : elements.priceFilter.value.split('-');
  state.minPrice = min;
  state.maxPrice = max;
  loadProducts();
}

elements.searchForm.addEventListener('submit', (event) => {
  event.preventDefault();
  state.search = elements.searchInput.value;
  loadProducts();
});
elements.searchInput.addEventListener('input', () => {
  clearTimeout(state.searchTimer);
  state.searchTimer = setTimeout(() => {
    state.search = elements.searchInput.value;
    loadProducts();
  }, 280);
});
elements.categoryList.addEventListener('click', (event) => {
  const button = event.target.closest('[data-category]');
  if (!button) return;
  state.selectedCategory = button.dataset.category;
  document.querySelectorAll('.category-button').forEach((item) => item.classList.toggle('is-active', item === button));
  loadProducts();
});
elements.sortFilter.addEventListener('change', () => { state.sortBy = elements.sortFilter.value; loadProducts(); });
elements.formatFilter.addEventListener('change', () => { state.format = elements.formatFilter.value; loadProducts(); });
elements.languageFilter.addEventListener('change', () => { state.language = elements.languageFilter.value; loadProducts(); });
elements.priceFilter.addEventListener('change', applyPriceFilter);
document.querySelector('#account-button').addEventListener('click', () => state.token ? signOut() : openLogin());
document.querySelector('#cart-button').addEventListener('click', openBasket);
document.querySelector('#close-basket').addEventListener('click', closeBasket);
document.querySelector('#drawer-scrim').addEventListener('click', closeBasket);
document.querySelector('#login-form').addEventListener('submit', signIn);

document.addEventListener('click', (event) => {
  const nav = event.target.closest('[data-view]');
  if (nav) {
    if (nav.dataset.view === 'wishlist') showWishlist();
    if (nav.dataset.view === 'writers') showWriters();
    if (nav.dataset.view === 'orders') showOrders();
  }
  const product = event.target.closest('[data-product]');
  if (product) openProduct(product.dataset.product);
  const add = event.target.closest('[data-add]');
  if (add) addToBasket(add.dataset.add);
  const wishlist = event.target.closest('[data-wishlist]');
  if (wishlist) toggleWishlist(wishlist.dataset.wishlist);
  const author = event.target.closest('[data-author]');
  if (author) showAuthor(author.dataset.author);
  const remove = event.target.closest('[data-remove]');
  if (remove) removeBasketItem(remove.dataset.remove);
  const close = event.target.closest('[data-close]');
  if (close) document.querySelector(`#${close.dataset.close}`).classList.add('is-hidden');
  if (event.target.classList.contains('overlay')) event.target.classList.add('is-hidden');
});

document.addEventListener('keydown', (event) => {
  if (event.key === 'Escape') {
    elements.loginOverlay.classList.add('is-hidden');
    elements.productOverlay.classList.add('is-hidden');
    closeBasket();
  }
});

async function start() {
  updateAccount();
  try {
    await loadCategories();
    await loadProducts({ initial: true });
    if (state.token) await loadBasket();
  } catch (error) {
    setStatus(`Book Worm could not connect to the catalogue. ${error.message}`);
  }
}

start();
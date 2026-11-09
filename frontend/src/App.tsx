import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import axios from 'axios'
import './App.css'

type Variation = { size: string; color: string; quantity: number }
type Product = {
  id: number
  name: string
  description?: string
  category: string
  price: number
  quantity: number
  imageUrl?: string
  variations?: Variation[]
}

type Client = { id: number; name: string; phone: string; email?: string; notes?: string }
type CustomerOrder = {
  id: number
  clientId: number
  clientName: string
  productId: number
  productName: string
  quantity: number
  expectedDate: string
  notes?: string
  status: 'PENDENTE'
}

type UserProfile = { name: string; phone: string; email: string }
type AuthMode = 'login' | 'register'
type Page = 'clients' | 'catalog' | 'products' | 'orders' | 'stock'

const navigationItems: { id: Page; label: string }[] = [
  { id: 'clients', label: 'Clientes' },
  { id: 'catalog', label: 'Consultar catálogo' },
  { id: 'products', label: 'Cadastrar produto' },
  { id: 'orders', label: 'Encomendas' },
]

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '/api',
  withCredentials: true,
})

function App() {
  const [page, setPage] = useState<Page>('products')
  const [menuOpen, setMenuOpen] = useState(false)
  const [user, setUser] = useState<UserProfile | null>(null)
  const [authLoading, setAuthLoading] = useState(true)
  const [authMode, setAuthMode] = useState<AuthMode>('login')
  const [authError, setAuthError] = useState('')
  const [authMessage, setAuthMessage] = useState('')
  const [authSaving, setAuthSaving] = useState(false)
  const [authForm, setAuthForm] = useState({
    name: '', phone: '', email: '', password: '', confirmPassword: '',
  })
  const [products, setProducts] = useState<Product[]>([])
  const [clients, setClients] = useState<Client[]>([])
  const [orders, setOrders] = useState<CustomerOrder[]>([])
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const [savingClient, setSavingClient] = useState(false)
  const [savingOrder, setSavingOrder] = useState(false)
  const [editingClientId, setEditingClientId] = useState<number | null>(null)
  const [clientForm, setClientForm] = useState({ name: '', phone: '', email: '', notes: '' })
  const [orderForm, setOrderForm] = useState({
    clientId: '', productId: '', quantity: '1', expectedDate: '', notes: '',
  })
  const [selectedProductId, setSelectedProductId] = useState<number | null>(null)
  const [stockVariations, setStockVariations] = useState<Variation[]>([])
  const [selectedStockVariationIndex, setSelectedStockVariationIndex] = useState(0)
  const [newStockQuantity, setNewStockQuantity] = useState('')
  const [savingStock, setSavingStock] = useState(false)
  const [stockUpdated, setStockUpdated] = useState(false)
  const [form, setForm] = useState({
    name: '', description: '', category: '', price: '', imageUrl: '',
  })
  const [variation, setVariation] = useState<Variation>({
    size: '', color: '', quantity: 0,
  })
  const [variations, setVariations] = useState<Variation[]>([])

  async function loadProducts() {
    try {
      const response = await api.get<Product[]>('/products')
      setProducts(response.data)
      setError('')
    } catch (requestError) {
      if (axios.isAxiosError(requestError) && requestError.response?.status === 401) {
        setUser(null)
        setAuthError('Sua sessão expirou. Entre novamente para continuar.')
      } else {
        setError('Não foi possível carregar os produtos. Verifique a conexão com o backend.')
      }
    }
  }

  async function loadClients() {
    try {
      const response = await api.get<Client[]>('/clients')
      setClients(response.data)
    } catch {
      setError('Não foi possível carregar as clientes. Tente novamente.')
    }
  }

  async function loadOrders() {
    try {
      const response = await api.get<CustomerOrder[]>('/orders')
      setOrders(response.data)
    } catch {
      setError('Não foi possível carregar as encomendas. Tente novamente.')
    }
  }

  async function refreshCsrfToken() {
    const response = await api.get<{ token: string }>('/auth/csrf')
    api.defaults.headers.common['X-CSRF-TOKEN'] = response.data.token
  }

  useEffect(() => {
    async function initializeSession() {
      try {
        await refreshCsrfToken()
        const response = await api.get<UserProfile>('/auth/me')
        setUser(response.data)
        await loadProducts()
      } catch (requestError) {
        if (axios.isAxiosError(requestError) && requestError.response?.status === 401) {
          setUser(null)
        } else {
          setAuthError('Não foi possível conectar ao backend. Tente novamente.')
        }
      } finally {
        setAuthLoading(false)
      }
    }

    void initializeSession()
  }, [])

  function navigateTo(nextPage: Page) {
    setPage(nextPage)
    setMenuOpen(false)
    setError('')
    setSelectedProductId(null)
    if (nextPage === 'clients') void loadClients()
    if (nextPage === 'orders') {
      void loadClients()
      void loadOrders()
    }
  }

  function startClientEdit(client: Client) {
    setEditingClientId(client.id)
    setClientForm({
      name: client.name,
      phone: client.phone,
      email: client.email ?? '',
      notes: client.notes ?? '',
    })
    setError('')
  }

  function cancelClientEdit() {
    setEditingClientId(null)
    setClientForm({ name: '', phone: '', email: '', notes: '' })
  }

  async function saveClient(event: FormEvent) {
    event.preventDefault()
    setSavingClient(true)
    setError('')
    try {
      if (editingClientId === null) {
        await api.post('/clients', clientForm)
      } else {
        await api.put(`/clients/${editingClientId}`, clientForm)
      }
      cancelClientEdit()
      await loadClients()
    } catch {
      setError('Não foi possível salvar a cliente. Confira os dados e tente novamente.')
    } finally {
      setSavingClient(false)
    }
  }

  async function deleteClient(client: Client) {
    if (!window.confirm(`Excluir o contato de ${client.name}?`)) return
    setError('')
    try {
      await api.delete(`/clients/${client.id}`)
      await loadClients()
    } catch (requestError) {
      const detail = axios.isAxiosError<{ detail?: string }>(requestError)
        ? requestError.response?.data?.detail
        : undefined
      setError(detail ?? 'Não foi possível excluir a cliente. Tente novamente.')
    }
  }

  function startOrderForClient(client?: Client) {
    const expectedDate = new Date()
    expectedDate.setDate(expectedDate.getDate() + 7)
    const localDate = [
      expectedDate.getFullYear(),
      String(expectedDate.getMonth() + 1).padStart(2, '0'),
      String(expectedDate.getDate()).padStart(2, '0'),
    ].join('-')
    setOrderForm({
      clientId: client ? String(client.id) : '',
      productId: '',
      quantity: '1',
      expectedDate: localDate,
      notes: '',
    })
    setPage('orders')
    setMenuOpen(false)
    setError('')
    void loadClients()
    void loadOrders()
  }

  async function saveOrder(event: FormEvent) {
    event.preventDefault()
    setSavingOrder(true)
    setError('')
    try {
      await api.post('/orders', {
        ...orderForm,
        clientId: Number(orderForm.clientId),
        productId: Number(orderForm.productId),
        quantity: Number(orderForm.quantity),
      })
      setOrderForm((current) => ({ ...current, notes: '' }))
      await loadOrders()
    } catch {
      setError('Não foi possível cadastrar a encomenda. Confira os dados e tente novamente.')
    } finally {
      setSavingOrder(false)
    }
  }

  function addVariation() {
    if (!variation.size.trim() || !variation.color.trim()) return
    setVariations((current) => [...current, { ...variation }])
    setVariation({ size: '', color: '', quantity: 0 })
  }

  async function addProduct(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    try {
      const quantity = variations.reduce((total, item) => total + item.quantity, 0)
      await api.post('/products', {
        ...form,
        price: Number(form.price),
        quantity,
        variations,
      })
      setForm({ name: '', description: '', category: '', price: '', imageUrl: '' })
      setVariations([])
      await loadProducts()
    } catch {
      setError('Confira os dados e tente novamente.')
    } finally {
      setSaving(false)
    }
  }

  async function submitAuthentication(event: FormEvent) {
    event.preventDefault()
    setAuthError('')
    setAuthMessage('')

    if (authMode === 'register' && authForm.password !== authForm.confirmPassword) {
      setAuthError('As senhas não conferem.')
      return
    }

    setAuthSaving(true)
    try {
      if (authMode === 'register') {
        await api.post('/auth/register', {
          name: authForm.name,
          phone: authForm.phone,
          email: authForm.email,
          password: authForm.password,
          confirmPassword: authForm.confirmPassword,
        })
        setAuthMode('login')
        setAuthForm((current) => ({ ...current, password: '', confirmPassword: '' }))
        setAuthMessage('Cadastro realizado. Entre com seu e-mail e senha.')
      } else {
        await api.post('/auth/login', {
          email: authForm.email,
          password: authForm.password,
        })
        const response = await api.get<UserProfile>('/auth/me')
        setUser(response.data)
        setAuthForm((current) => ({ ...current, password: '', confirmPassword: '' }))
        setPage('products')
        await loadProducts()
      }
    } catch (requestError) {
      if (axios.isAxiosError<{ detail?: string; message?: string }>(requestError)) {
        const status = requestError.response?.status
        const serverMessage = requestError.response?.data?.detail
          ?? requestError.response?.data?.message
        setAuthError(serverMessage
          ?? (status === 401
            ? 'E-mail ou senha inválidos.'
            : status === 409
              ? 'Este e-mail já está cadastrado.'
              : 'Não foi possível concluir a solicitação. Confira os dados e tente novamente.'))
      } else {
        setAuthError('Não foi possível conectar ao backend. Tente novamente.')
      }
    } finally {
      setAuthSaving(false)
    }
  }

  async function logout() {
    setAuthError('')
    try {
      await api.post('/auth/logout')
      setUser(null)
      setProducts([])
      setClients([])
      setOrders([])
      setPage('products')
      setMenuOpen(false)
      setAuthForm({ name: '', phone: '', email: '', password: '', confirmPassword: '' })
      await refreshCsrfToken()
    } catch {
      setAuthError('Não foi possível encerrar a sessão. Tente novamente.')
    }
  }

  function startStockUpdate() {
    const product = products.find((item) => item.id === selectedProductId)
    if (!product) return

    const currentVariations = product.variations?.length
      ? product.variations.map((item) => ({ ...item }))
      : [{ size: 'Único', color: product.category, quantity: product.quantity }]
    setStockVariations(currentVariations)
    setSelectedStockVariationIndex(0)
    setNewStockQuantity(String(currentVariations[0].quantity))
    setStockUpdated(false)
    setPage('stock')
    setError('')
  }

  async function updateStock(event: FormEvent) {
    event.preventDefault()
    const product = products.find((item) => item.id === selectedProductId)
    const currentVariation = stockVariations[selectedStockVariationIndex]
    const quantity = Number(newStockQuantity)
    if (!product || !currentVariation || !Number.isInteger(quantity) || quantity < 0) return

    setSavingStock(true)
    try {
      const updatedVariations = stockVariations.map((item, index) =>
        index === selectedStockVariationIndex ? { ...item, quantity } : item)
      const totalQuantity = updatedVariations.reduce((total, item) => total + item.quantity, 0)
      const response = await api.put<Product>(`/products/${product.id}`, {
        name: product.name,
        description: product.description,
        category: product.category,
        price: product.price,
        imageUrl: product.imageUrl,
        quantity: totalQuantity,
        variations: updatedVariations,
      })
      setProducts((current) => current.map((item) =>
        item.id === response.data.id ? response.data : item))
      setStockVariations(response.data.variations ?? updatedVariations)
      setNewStockQuantity(String(quantity))
      setStockUpdated(true)
      setError('')
    } catch {
      setError('Não foi possível atualizar o estoque. Tente novamente.')
    } finally {
      setSavingStock(false)
    }
  }

  function returnToCatalog() {
    setPage('catalog')
    setSelectedProductId(null)
    setStockUpdated(false)
    setError('')
  }

  const selectedProduct = products.find((item) => item.id === selectedProductId)
  const selectedStockVariation = stockVariations[selectedStockVariationIndex]

  if (authLoading) {
    return <main className="auth-shell"><p className="auth-loading">Carregando...</p></main>
  }

  if (!user) {
    return (
      <main className="auth-shell">
        <section className="auth-card">
          <div className="auth-logo-wrap">
            <img
              className="auth-logo"
              src="/logo-maos-conectadas.png"
              alt="Mãos Conectadas — Artesanato e Tecnologia"
            />
            <p className="auth-tagline">Organize seus produtos com simplicidade</p>
          </div>
          <h2>{authMode === 'login' ? 'Acesse sua conta' : 'Cadastre-se'}</h2>
          <p className="auth-intro">
            {authMode === 'login'
              ? 'Entre para gerenciar seu catálogo.'
              : 'Crie sua conta para começar a organizar seus produtos.'}
          </p>
          {authError && <p className="error" role="alert">{authError}</p>}
          {authMessage && <p className="auth-message" role="status">{authMessage}</p>}
          <form className="auth-form" onSubmit={submitAuthentication}>
            {authMode === 'register' && (
              <>
                <label>Nome
                  <input
                    autoComplete="name"
                    maxLength={120}
                    required
                    value={authForm.name}
                    onChange={(event) => setAuthForm({ ...authForm, name: event.target.value })}
                  />
                </label>
                <label>Celular
                  <input
                    autoComplete="tel"
                    maxLength={30}
                    required
                    type="tel"
                    value={authForm.phone}
                    onChange={(event) => setAuthForm({ ...authForm, phone: event.target.value })}
                  />
                </label>
              </>
            )}
            <label>E-mail
              <input
                autoComplete="email"
                maxLength={254}
                required
                type="email"
                value={authForm.email}
                onChange={(event) => setAuthForm({ ...authForm, email: event.target.value })}
              />
            </label>
            <label>Senha
              <input
                autoComplete={authMode === 'login' ? 'current-password' : 'new-password'}
                maxLength={72}
                minLength={8}
                required
                type="password"
                value={authForm.password}
                onChange={(event) => setAuthForm({ ...authForm, password: event.target.value })}
              />
            </label>
            {authMode === 'register' && (
              <label>Confirme a senha
                <input
                  autoComplete="new-password"
                  maxLength={72}
                  minLength={8}
                  required
                  type="password"
                  value={authForm.confirmPassword}
                  onChange={(event) => setAuthForm({ ...authForm, confirmPassword: event.target.value })}
                />
              </label>
            )}
            <button className="save-button" type="submit" disabled={authSaving}>
              {authSaving
                ? 'Aguarde...'
                : authMode === 'login'
                  ? 'Entrar'
                  : 'Cadastrar'}
            </button>
          </form>
          <div className="auth-switch-wrap">
            <button
              className="auth-switch"
              type="button"
              onClick={() => {
                setAuthMode(authMode === 'login' ? 'register' : 'login')
                setAuthError('')
                setAuthMessage('')
              }}
            >
              {authMode === 'login' ? 'Cadastre-se aqui' : 'Já tem uma conta? Entrar'}
            </button>
          </div>
        </section>
      </main>
    )
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <h1 className="brand">MÃOS CONECTADAS</h1>
        {page !== 'stock' && <div className="menu-container">
          <button
            className="menu-toggle"
            type="button"
            aria-label={menuOpen ? 'Fechar menu' : 'Abrir menu'}
            aria-expanded={menuOpen}
            aria-controls="main-navigation"
            onClick={() => setMenuOpen((open) => !open)}
          >
            <span />
            <span />
            <span />
          </button>
          {menuOpen && (
            <nav className="navigation-menu" id="main-navigation" aria-label="Navegação principal">
              <p className="greeting">Olá, {user.name}</p>
              {navigationItems.map((item) => (
                <button
                  className={page === item.id ? 'navigation-item active' : 'navigation-item'}
                  key={item.id}
                  type="button"
                  aria-current={page === item.id ? 'page' : undefined}
                  onClick={() => navigateTo(item.id)}
                >
                  {item.label}
                </button>
              ))}
              <button className="navigation-item logout-item" type="button" onClick={() => void logout()}>
                Sair
              </button>
            </nav>
          )}
        </div>}
      </header>

      <section className="page-heading">
        <h2>{page === 'stock'
          ? 'Atualizar estoque'
          : navigationItems.find((item) => item.id === page)?.label}</h2>
        {page === 'products' && <p>Preencha os dados para adicionar ao catálogo.</p>}
        {page === 'stock' && <p>Altere a quantidade de uma variação do produto.</p>}
        {page === 'clients' && <p>Organize seus contatos e anotações sobre cada cliente.</p>}
        {page === 'orders' && <p>Cadastre e acompanhe as encomendas feitas pelas clientes.</p>}
      </section>

      {error && <p className="error">{error}</p>}

      {page === 'products' && (
        <section className="content">
          <div className="form-card">
            <h3>Informações do produto</h3>
            <form className="product-form" onSubmit={addProduct}>
              <label>Nome do produto *
                <input required placeholder="Bolsa artesanal" value={form.name}
                  onChange={(event) => setForm({ ...form, name: event.target.value })} />
              </label>
              <label>Descrição
                <textarea placeholder="Bolsa de crochê para uso diário" value={form.description}
                  onChange={(event) => setForm({ ...form, description: event.target.value })} />
              </label>
              <div className="form-row">
                <label>Categoria *
                  <input required placeholder="Crochê" value={form.category}
                    onChange={(event) => setForm({ ...form, category: event.target.value })} />
                </label>
                <label>Preço (R$) *
                  <input required min="0.01" step="0.01" type="number" placeholder="65,00" value={form.price}
                    onChange={(event) => setForm({ ...form, price: event.target.value })} />
                </label>
              </div>
              <label>Foto do produto (opcional)
                <div className="image-input">
                  <span>＋</span>
                  <input type="url" placeholder="Adicionar imagem" value={form.imageUrl}
                    onChange={(event) => setForm({ ...form, imageUrl: event.target.value })} />
                </div>
              </label>

              <div className="variations">
                <h3>Variações</h3>
                <div className="variation-row">
                  <label>Tamanho
                    <input placeholder="Único" value={variation.size}
                      onChange={(event) => setVariation({ ...variation, size: event.target.value })} />
                  </label>
                  <label>Cor
                    <input placeholder="Bege" value={variation.color}
                      onChange={(event) => setVariation({ ...variation, color: event.target.value })} />
                  </label>
                  <label>Quantidade
                    <input min="0" type="number" value={variation.quantity || ''}
                      onChange={(event) => setVariation({ ...variation, quantity: Number(event.target.value) })} />
                  </label>
                </div>
                <button className="add-variation" type="button" onClick={addVariation}>＋ Adicionar outra variação</button>
                {variations.map((item, index) => (
                  <div className="variation-preview" key={`${item.color}-${item.size}-${index}`}>
                    {item.size} · {item.color} · {item.quantity} un.
                  </div>
                ))}
              </div>

              <button className="save-button" type="submit" disabled={saving}>
                {saving ? 'Salvando...' : 'Salvar produto'}
              </button>
            </form>
          </div>
        </section>
      )}

      {page === 'catalog' && (
        <div className="catalog-column">
          <h2>Meu catálogo</h2>
          <span className="product-count">{products.length} produtos</span>
          <div className="catalog-list">
            {products.map((product) => (
              <article className="catalog-card" key={product.id}>
                <label className="product-selection">
                  <input
                    type="checkbox"
                    checked={selectedProductId === product.id}
                    onChange={(event) => {
                      setSelectedProductId(event.target.checked ? product.id : null)
                      setError('')
                    }}
                  />
                  <span>Selecionar produto</span>
                </label>
                <div className="catalog-card-heading">
                  <h3>{product.name}</h3>
                  <strong>{new Intl.NumberFormat('pt-BR', {
                    style: 'currency', currency: 'BRL',
                  }).format(product.price)}</strong>
                </div>
                <p className="catalog-meta">
                  {product.category}
                  {product.variations?.[0]?.size && ` · Tamanho ${product.variations[0].size}`}
                </p>
                <div className="variation-list">
                  {(product.variations?.length ? product.variations : [{
                    size: 'Único', color: product.category, quantity: product.quantity,
                  }]).map((item, index) => (
                    <span key={`${item.color}-${item.size}-${index}`}>
                      {item.color}<b>{item.quantity} un.</b>
                    </span>
                  ))}
                </div>
              </article>
            ))}
          </div>
          <button
            className="save-button stock-update-button"
            type="button"
            disabled={selectedProductId === null || savingStock}
            onClick={startStockUpdate}
          >
            Atualizar Estoque
          </button>
        </div>
      )}

      {page === 'stock' && selectedProduct && selectedStockVariation && (
        <section className="stock-page">
          <div className="stock-product-card">
            <p className="stock-eyebrow">PRODUTO SELECIONADO</p>
            <h3>{selectedProduct.name}</h3>
            {selectedProduct.description && <p>{selectedProduct.description}</p>}
            <p className="stock-product-meta">
              Categoria: {selectedProduct.category} · {new Intl.NumberFormat('pt-BR', {
                style: 'currency', currency: 'BRL',
              }).format(selectedProduct.price)}
            </p>
            {selectedProduct.imageUrl && (
              <img className="stock-product-image" src={selectedProduct.imageUrl} alt={selectedProduct.name} />
            )}
            <div className="stock-variation">
              <h4>Variação</h4>
              {stockVariations.length > 1 ? (
                <select
                  aria-label="Variação do produto"
                  value={selectedStockVariationIndex}
                  onChange={(event) => {
                    const index = Number(event.target.value)
                    setSelectedStockVariationIndex(index)
                    setNewStockQuantity(String(stockVariations[index].quantity))
                    setStockUpdated(false)
                  }}
                >
                  {stockVariations.map((item, index) => (
                    <option key={`${item.size}-${item.color}-${index}`} value={index}>
                      Tamanho {item.size} / {item.color}
                    </option>
                  ))}
                </select>
              ) : (
                <p>Tamanho {selectedStockVariation.size} / {selectedStockVariation.color}</p>
              )}
            </div>
          </div>

          <form className="stock-form" onSubmit={updateStock}>
            <div className="stock-current">
              <span>Quantidade atual</span>
              <strong>{selectedStockVariation.quantity} unidades</strong>
            </div>
            <label htmlFor="new-stock-quantity">Nova quantidade</label>
            <input
              id="new-stock-quantity"
              min="0"
              required
              step="1"
              type="number"
              value={newStockQuantity}
              onChange={(event) => {
                setNewStockQuantity(event.target.value)
                setStockUpdated(false)
              }}
            />
            <p className="stock-hint">Informe um número inteiro igual ou maior que zero.</p>
            <button className="save-button" type="submit" disabled={savingStock}>
              {savingStock ? 'Salvando...' : 'Salvar atualização'}
            </button>
          </form>

          {stockUpdated && (
            <div className="stock-success" role="status">
              <strong>✓ &nbsp; Estoque atualizado com sucesso</strong>
              <p>A variação {selectedStockVariation.color} agora possui {selectedStockVariation.quantity} unidades.</p>
              {stockVariations
                .filter((_, index) => index !== selectedStockVariationIndex)
                .map((item, index) => (
                  <p key={`${item.size}-${item.color}-${index}`}>
                    A variação {item.color} permanece com {item.quantity} unidades.
                  </p>
                ))}
            </div>
          )}

          <button className="back-to-catalog" type="button" onClick={returnToCatalog}>
            ← Voltar ao catálogo
          </button>
        </section>
      )}

      {page === 'clients' && (
        <section className="feature-page">
          <div className="form-card">
            <h3>{editingClientId === null ? 'Novo contato' : 'Editar contato'}</h3>
            <form className="product-form" onSubmit={saveClient}>
              <label>Nome *
                <input required maxLength={120} value={clientForm.name}
                  onChange={(event) => setClientForm({ ...clientForm, name: event.target.value })} />
              </label>
              <div className="form-row">
                <label>Celular *
                  <input required maxLength={30} type="tel" value={clientForm.phone}
                    onChange={(event) => setClientForm({ ...clientForm, phone: event.target.value })} />
                </label>
                <label>E-mail
                  <input maxLength={254} type="email" value={clientForm.email}
                    onChange={(event) => setClientForm({ ...clientForm, email: event.target.value })} />
                </label>
              </div>
              <label>Observações
                <textarea maxLength={1000} placeholder="Preferências, medidas ou outras informações"
                  value={clientForm.notes}
                  onChange={(event) => setClientForm({ ...clientForm, notes: event.target.value })} />
              </label>
              <button className="save-button" type="submit" disabled={savingClient}>
                {savingClient ? 'Salvando...' : editingClientId === null ? 'Salvar contato' : 'Atualizar contato'}
              </button>
              {editingClientId !== null && (
                <button className="back-to-catalog" type="button" onClick={cancelClientEdit}>
                  Cancelar edição
                </button>
              )}
            </form>
          </div>

          <div className="client-section">
            <div className="section-heading">
              <h3>Minha agenda</h3>
              <span>{clients.length} contatos</span>
            </div>
            {clients.length === 0 ? (
              <p className="empty-state">Ainda não há contatos cadastrados.</p>
            ) : (
              <div className="client-list">
                {clients.map((client) => (
                  <article className="client-card" key={client.id}>
                    <div className="client-card-heading">
                      <h4>{client.name}</h4>
                      <a href={`tel:${client.phone}`}>Ligar</a>
                    </div>
                    <p className="client-contact">{client.phone}{client.email && ` · ${client.email}`}</p>
                    {client.notes && <p className="client-notes">{client.notes}</p>}
                    <div className="client-actions">
                      <button className="save-button" type="button"
                        onClick={() => startOrderForClient(client)}>Nova encomenda</button>
                      <button className="back-to-catalog" type="button"
                        onClick={() => startClientEdit(client)}>Editar</button>
                      <button className="back-to-catalog" type="button"
                        onClick={() => void deleteClient(client)}>Excluir</button>
                    </div>
                  </article>
                ))}
              </div>
            )}
          </div>
        </section>
      )}

      {page === 'orders' && (
        <section className="feature-page">
          <div className="form-card">
            <h3>Nova encomenda</h3>
            {clients.length === 0 && (
              <p className="empty-state">
                Cadastre uma cliente antes de criar a encomenda.
                <button className="text-button" type="button" onClick={() => navigateTo('clients')}>
                  Ir para Clientes
                </button>
              </p>
            )}
            {products.length === 0 && (
              <p className="empty-state">
                Cadastre um produto antes de criar a encomenda.
                <button className="text-button" type="button" onClick={() => navigateTo('products')}>
                  Ir para Cadastrar produto
                </button>
              </p>
            )}
            {clients.length > 0 && products.length > 0 && (
              <form className="product-form" onSubmit={saveOrder}>
                <label>Cliente *
                  <select required value={orderForm.clientId}
                    onChange={(event) => setOrderForm({ ...orderForm, clientId: event.target.value })}>
                    <option value="">Selecione uma cliente</option>
                    {clients.map((client) => (
                      <option key={client.id} value={client.id}>{client.name}</option>
                    ))}
                  </select>
                </label>
                <label>Produto *
                  <select required value={orderForm.productId}
                    onChange={(event) => setOrderForm({ ...orderForm, productId: event.target.value })}>
                    <option value="">Selecione um produto</option>
                    {products.map((product) => (
                      <option key={product.id} value={product.id}>{product.name}</option>
                    ))}
                  </select>
                </label>
                <div className="form-row">
                  <label>Quantidade *
                    <input required min="1" step="1" type="number" value={orderForm.quantity}
                      onChange={(event) => setOrderForm({ ...orderForm, quantity: event.target.value })} />
                  </label>
                  <label>Previsão de entrega *
                    <input required type="date" value={orderForm.expectedDate}
                      onChange={(event) => setOrderForm({ ...orderForm, expectedDate: event.target.value })} />
                  </label>
                </div>
                <label>Observações
                  <textarea maxLength={1000} placeholder="Detalhes ou combinações da encomenda"
                    value={orderForm.notes}
                    onChange={(event) => setOrderForm({ ...orderForm, notes: event.target.value })} />
                </label>
                <button className="save-button" type="submit" disabled={savingOrder}>
                  {savingOrder ? 'Salvando...' : 'Cadastrar encomenda'}
                </button>
              </form>
            )}
          </div>

          <div className="client-section">
            <div className="section-heading">
              <h3>Encomendas cadastradas</h3>
              <span>{orders.length} encomendas</span>
            </div>
            {orders.length === 0 ? (
              <p className="empty-state">Ainda não há encomendas cadastradas.</p>
            ) : (
              <div className="order-list">
                {orders.map((order) => (
                  <article className="order-card" key={order.id}>
                    <div className="client-card-heading">
                      <h4>{order.productName}</h4>
                      <span className="order-status">Pendente</span>
                    </div>
                    <p className="client-contact">Cliente: {order.clientName} · Quantidade: {order.quantity}</p>
                    <p className="client-contact">
                      Previsão: {new Intl.DateTimeFormat('pt-BR', { dateStyle: 'long' })
                        .format(new Date(`${order.expectedDate}T12:00:00`))}
                    </p>
                    {order.notes && <p className="client-notes">{order.notes}</p>}
                  </article>
                ))}
              </div>
            )}
          </div>
        </section>
      )}
    </main>
  )
}

export default App

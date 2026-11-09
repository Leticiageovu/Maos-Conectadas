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
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
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

      {(page === 'clients' || page === 'orders') && (
        <section className="coming-soon" aria-live="polite">
          <h3>{page === 'clients' ? 'Cadastro de clientes' : 'Controle de encomendas'}</h3>
          <p>Esta funcionalidade estará disponível em breve.</p>
        </section>
      )}
    </main>
  )
}

export default App

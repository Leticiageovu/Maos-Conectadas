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

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '/api',
})

function App() {
  const [products, setProducts] = useState<Product[]>([])
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
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
    } catch {
      setError('Não foi possível conectar ao backend.')
    }
  }

  useEffect(() => { void loadProducts() }, [])

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

  return (
    <main>
      <h1 className="brand">MÃOS CONECTADAS</h1>

      <section className="page-heading">
        <h2>Cadastrar produto</h2>
        <p>Preencha os dados para adicionar ao catálogo.</p>
      </section>

      {error && <p className="error">{error}</p>}

      <section className="content">
        <aside>
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
        </aside>

        <div className="catalog-column">
          <h2>Meu catálogo</h2>
          <span className="product-count">{products.length} produtos</span>
          <div className="catalog-list">
            {products.map((product) => (
              <article className="catalog-card" key={product.id}>
                <h3>{product.name}</h3>
                <strong>{new Intl.NumberFormat('pt-BR', {
                  style: 'currency', currency: 'BRL',
                }).format(product.price)}</strong>
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
        </div>
      </section>
    </main>
  )
}

export default App

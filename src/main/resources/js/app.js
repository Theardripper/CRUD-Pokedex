const list = document.getElementById('pokemon-list');
const form = document.getElementById('form-pokemon');

let editingId = null;

if (list && form) {
    function getPokemonImageUrl(pokemonId) {
        const id = pokemonId || Math.floor(Math.random() * 151) + 1;
        return `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${id}.png`;
    }

    async function carregarPokemons() {
        try {
            const res = await fetch('/api/pokemons');
            const data = await res.json();
            list.innerHTML = '';

            if (data.length === 0) {
                list.innerHTML = '<p style="text-align: center; margin-top: 30px; font-style: italic;">Nenhum Pokémon capturado.</p>';
                return;
            }

            data.forEach(pokemon => {
                const li = document.createElement('li');
                li.classList.add('pokemon-card'); // CLASSE DO NOSSO CSS

                const pokemonId = pokemon.pokedex_number || pokemon.id || Math.floor(Math.random() * 151) + 1;
                const imageUrl = getPokemonImageUrl(pokemonId);

                li.innerHTML = `
                    <div class="card-header-actions">
                        <label class="left-checkbox">
                            <input type="checkbox" class="chk-catched" ${pokemon.catched ? 'checked' : ''} />
                            <span>Capturado</span>
                        </label>
                        <div class="pokemon-actions">
                            <button class="button btn-edit">Editar</button>
                            <button class="button btn-delete">Deletar</button>
                        </div>
                    </div>

                    <div class="pokemon-card-image" style="background-image: url('${imageUrl}')"></div>
                    
                    <h2>${escapeHtml(pokemon.name)}</h2>
                    <p class="pokemon-type"><strong>Tipo:</strong> ${escapeHtml(pokemon.type)}</p>
                    <div class="pokemon-meta-group">
                        <p><strong>Peso:</strong> ${pokemon.weight ?? '-'}</p>
                        <p><strong>Altura:</strong> ${pokemon.height ?? '-'}</p>
                    </div>
                `;
                list.appendChild(li);

                const chk = li.querySelector('.chk-catched');
                const btnEdit = li.querySelector('.btn-edit');
                const btnDelete = li.querySelector('.btn-delete');

                chk.addEventListener('change', () => onToggleCatch(pokemon.id, chk.checked));
                btnEdit.addEventListener('click', () => startEdit(pokemon.id));
                btnDelete.addEventListener('click', () => deletePokemon(pokemon.id));
            });
        } catch (err) {
            console.error('Erro ao carregar pokemons', err);
            list.innerHTML = '<li>Erro ao carregar pokémons</li>';
        }
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/[&<>"']/g, s => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[s]));
    }

    async function onToggleCatch(id, novoStatus) {
        try {
            await fetch(`/api/pokemons/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ catched: novoStatus })
            });
            await carregarPokemons();
        } catch (err) {
            console.error('Erro ao alternar catched', err);
            alert('Erro ao alternar estado do Pokémon.');
        }
    }

    async function deletePokemon(id) {
        if (!confirm('Tem certeza que deseja deletar este Pokémon?')) return;
        try {
            await fetch(`/api/pokemons/${id}`, { method: 'DELETE' });
            await carregarPokemons();
        } catch (err) {
            console.error('Erro ao deletar', err);
            alert('Erro ao deletar Pokémon.');
        }
    }

    async function startEdit(id) {
        try {
            const res = await fetch(`/api/pokemons/${id}`);
            if (!res.ok) { alert('Pokémon não encontrado.'); return; }
            const p = await res.json();
            document.getElementById('name').value = p.name || '';
            document.getElementById('type').value = p.type || '';
            document.getElementById('weight').value = p.weight ?? '';
            document.getElementById('height').value = p.height ?? '';
            editingId = id;
            form.querySelector('button[type="submit"]').textContent = 'Salvar';
        } catch (err) {
            console.error('Erro ao iniciar edição', err);
            alert('Erro ao carregar dados para edição.');
        }
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('name').value.trim();
        const type = document.getElementById('type').value.trim();
        const weightRaw = document.getElementById('weight').value.trim();
        const heightRaw = document.getElementById('height').value.trim();

        const weight = weightRaw === '' ? null : parseFloat(weightRaw);
        const height = heightRaw === '' ? null : parseFloat(heightRaw);

        const payload = { name, type, weight, height, catched: false };

        try {
            if (editingId) {
                await fetch(`/api/pokemons/${editingId}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ id: editingId, ...payload })
                });
                editingId = null;
                form.querySelector('button[type="submit"]').textContent = 'Adicionar';
            } else {
                await fetch('/api/pokemons', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
            }
            form.reset();
            await carregarPokemons();
        } catch (err) {
            console.error('Erro no salvar/criar', err);
            alert('Erro ao salvar Pokémon.');
        }
    });

    carregarPokemons();
}
const form = document.getElementById('loginForm');

form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const fd = new FormData(form);
    const payload = {
        email: fd.get('email'),
        password: fd.get('password')
    };

    try {
        const resp = await fetch('/api/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await resp.json();

        if (resp.ok) {
            localStorage.setItem("logado", "true");
            localStorage.setItem("usuario", data.name || "Treinador");
            alert('Bem-vindo, ' + (data.name || 'treinador') + '!');
            
            window.location.href = 'pokedex.html';

        } else {
            alert(data.error || "Usuário ou senha incorretos!");
        }
    } catch (err) {
        console.error(err);
        alert('Erro ao conectar ao servidor.');
    }
});
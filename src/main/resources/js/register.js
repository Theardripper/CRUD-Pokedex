const rf = document.getElementById('registerForm');

rf.addEventListener('submit', async e => {
    e.preventDefault();
    const fd = new FormData(rf);
    const payload = {
        name: fd.get('name'),
        email: fd.get('email'),
        password: fd.get('password')
    };

    try {
        const resp = await fetch('/api/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await resp.json();
        if (resp.ok) {
            alert('Registrado com sucesso. Faça login.');

            window.location.href = 'login.html';

        } else {
            alert('Erro: ' + (data.error || data.message));
        }
    } catch (err) {
        console.error(err);
        alert('Erro ao conectar ao servidor.');
    }
});
const BACKEND_URL = 'http://localhost:8080/api/v1/download/stream';

document.getElementById('downloadForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    // Captura de datos ingresados
    const urlInput = document.getElementById('url').value;
    const qualitySelect = document.getElementById('quality').value;
    
    const btnSubmit = document.getElementById('btnSubmit');
    const btnIcon = document.getElementById('btnIcon');
    const btnText = document.getElementById('btnText');
    
    const statusCard = document.getElementById('statusCard');
    const statusIcon = document.getElementById('statusIcon');
    const statusText = document.getElementById('statusText');
    const progressBar = document.getElementById('progressBar');

    btnSubmit.disabled = true;
    btnIcon.className = 'fa-solid fa-spinner fa-spin'; 
    btnText.innerText = 'Descargando...';

    statusCard.className = 'status-card status-info';
    statusIcon.className = 'fa-solid fa-circle-notch fa-spin';
    statusText.innerText = 'El servidor está descargando el video y uniendo las pistas con FFmpeg. Por favor, espera unos instantes...';
    statusCard.classList.remove('hidden');
    progressBar.classList.remove('hidden');

    try {
        const response = await fetch(BACKEND_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                url: urlInput,
                quality: qualitySelect
            })
        });

        if (!response.ok) {
            throw new Error('No se pudo procesar la descarga de este video en el servidor.');
        }

        const videoBlob = await response.blob();

        const nombreArchivo = `Video_YouTube_${qualitySelect}p_${Date.now()}.mp4`;

        const downloadUrl = window.URL.createObjectURL(videoBlob);
        const linkOculto = document.createElement('a');
        linkOculto.href = downloadUrl;
        linkOculto.download = nombreArchivo;
        
        document.body.appendChild(linkOculto);
        linkOculto.click();
        document.body.removeChild(linkOculto);
        
        window.URL.revokeObjectURL(downloadUrl);

        statusCard.className = 'status-card status-success';
        statusIcon.className = 'fa-solid fa-circle-check';
        statusText.innerText = '¡Éxito! El video se ha procesado y guardado en tu carpeta de descargas.';
        progressBar.classList.add('hidden');

        document.getElementById('url').value = '';

    } catch (error) {
        // 7. Estado Visual: Manejo de Errores
        console.error('Error en descarga:', error);
        statusCard.className = 'status-card status-error';
        statusIcon.className = 'fa-solid fa-circle-exclamation';
        statusText.innerText = 'Hubo un error al procesar el archivo. Revisa la consola del navegador.';
        progressBar.classList.add('hidden');
    } finally {
        btnSubmit.disabled = false;
        btnIcon.className = 'fa-solid fa-cloud-arrow-down';
        btnText.innerText = 'Comenzar Descarga';
    }
});
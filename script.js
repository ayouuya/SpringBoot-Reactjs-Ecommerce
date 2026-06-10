/**
 * ==========================================================================
 * DIGITECH PRESENTATION - JAVASCRIPT LOGIC (script.js)
 * High-performance, modern slider with GSAP transitions, keyboard navigation,
 * dynamic image directory fallback, and zoom/pan interactive lightbox.
 * ==========================================================================
 */

document.addEventListener('DOMContentLoaded', () => {

  // --- DOM Elements ---
  const slides = document.querySelectorAll('.slide');
  const prevBtn = document.getElementById('prevBtn');
  const nextBtn = document.getElementById('nextBtn');
  const progressBar = document.getElementById('progressBar');
  
  // Sidebar elements
  const menuToggleBtn = document.getElementById('menuToggleBtn');
  const sidebarMenu = document.getElementById('sidebarMenu');
  const sidebarCloseBtn = document.getElementById('sidebarCloseBtn');
  const sidebarOverlay = document.getElementById('sidebarOverlay');
  const sidebarItems = document.querySelectorAll('.sidebar-item');
  
  // Lightbox elements
  const umlLightbox = document.getElementById('umlLightbox');
  const lightboxClose = document.getElementById('lightboxClose');
  const lightboxTitle = document.getElementById('lightboxTitle');
  const lightboxDesc = document.getElementById('lightboxDesc');
  const lightboxImage = document.getElementById('lightboxImage');
  const lightboxImgWrapper = document.getElementById('lightboxImgWrapper');
  const zoomInBtn = document.getElementById('zoomInBtn');
  const zoomOutBtn = document.getElementById('zoomOutBtn');
  const zoomResetBtn = document.getElementById('zoomResetBtn');
  const zoomIndicator = document.getElementById('zoomIndicator');

  // --- Presentation State ---
  let currentSlide = 0;
  const totalSlides = slides.length;
  let isTransitioning = false;

  // --- Lightbox Zoom/Pan State ---
  let zoomLevel = 1.0;
  const zoomStep = 0.15;
  const maxZoom = 3.0;
  const minZoom = 0.5;
  
  let isDragging = false;
  let panX = 0;
  let panY = 0;
  let startX = 0;
  let startY = 0;

  // UML Detail Explanations for Lightbox
  const umlDetails = {
    usecase: {
      title: "Diagramme de Cas d'Utilisation UML - Portée Fonctionnelle",
      desc: "Cartographie complète des interactions. Permet de visualiser les rôles des 3 acteurs physiques (Visiteur, Client Connecté, Administrateur) et des 2 systèmes externes (PayPal, OpenRouter API) avec les cas d'utilisation clés."
    },
    class: {
      title: "Diagramme de Classes Structurel - Modèle de Données JPA",
      desc: "Représentation statique de la base H2. Met en évidence la persistance des entités (AppUser, Product, Cart, CartItem, Order, OrderItem, Payment, ChatMessage) et leurs cardinalités réelles."
    },
    seq_auth: {
      title: "Séquence UML : Connexion \& Interception de Session",
      desc: "Détaille le stockage du profil dans le localStorage et le fonctionnement de l'intercepteur de requêtes Axios qui injecte automatiquement les en-têtes d'authentification pour chaque requête HTTP sortante."
    },
    seq_order: {
      title: "Séquence UML : Processus de Commande \& Validation",
      desc: "Illustre l'atomicité du Checkout : contrôle de validité utilisateur, chargement du panier, approbation de la passerelle PayPal, décrémentation des stocks physiques et persistance de la facture."
    },
    seq_chat: {
      title: "Séquence UML : Assistant Conversationnel (Chatbot IA)",
      desc: "Détaille le flux du chat : persistance de la question client, assemblage de l'historique tronqué aux 20 derniers messages, envoi non-bloquant à OpenRouter via WebClient, persistance et restitution de la réponse."
    }
  };

  // --- Initialization ---
  updateUI();
  animateSlideIn(0); // Animate first slide on load

  // --- Slide Navigation ---
  function goToSlide(index, direction = 'next') {
    if (index < 0 || index >= totalSlides || isTransitioning || index === currentSlide) return;
    
    isTransitioning = true;
    const oldSlide = slides[currentSlide];
    const newSlide = slides[index];
    
    // Deactivate old slide state in CSS
    oldSlide.classList.remove('active');
    if (direction === 'next') {
      oldSlide.classList.add('exit-left');
    } else {
      oldSlide.classList.add('exit-right');
    }
    
    // Prepare new slide
    newSlide.classList.remove('exit-left', 'exit-right');
    newSlide.classList.add('active');
    
    // Update active index
    currentSlide = index;
    
    // Trigger GSAP entrance animations
    animateSlideIn(currentSlide);
    
    // Remove exit classes from old slide after transition completes
    setTimeout(() => {
      oldSlide.classList.remove('exit-left', 'exit-right');
      isTransitioning = false;
    }, 500);

    updateUI();
  }

  function animateSlideIn(index) {
    const slide = slides[index];
    const content = slide.querySelector('.slide-content') || slide.querySelector('.cover-card');
    
    if (window.gsap && content) {
      // General slide animations
      gsap.fromTo(content, 
        { opacity: 0, y: 30, scale: 0.96 }, 
        { opacity: 1, y: 0, scale: 1, duration: 0.65, ease: 'power2.out' }
      );
      
      // Animate interior cards/list items for premium effect
      const animateElements = slide.querySelectorAll('.glass-card, .bullet-item, .tech-mini-card, .cover-badge, .cover-meta-item');
      if (animateElements.length > 0) {
        gsap.fromTo(animateElements, 
          { opacity: 0, y: 15 }, 
          { opacity: 1, y: 0, duration: 0.5, stagger: 0.08, delay: 0.2, ease: 'power2.out' }
        );
      }
    }
  }

  function updateUI() {
    // Enable/Disable navigation buttons
    prevBtn.disabled = (currentSlide === 0);
    nextBtn.disabled = (currentSlide === totalSlides - 1);

    // Update Progress Bar
    const progressPercent = (currentSlide / (totalSlides - 1)) * 100;
    progressBar.style.width = `${progressPercent}%`;

    // Update Sidebar Active Item
    sidebarItems.forEach((item, idx) => {
      if (idx === currentSlide) {
        item.classList.add('active');
      } else {
        item.classList.remove('active');
      }
    });
  }

  // --- Button Listeners ---
  prevBtn.addEventListener('click', () => goToSlide(currentSlide - 1, 'prev'));
  nextBtn.addEventListener('click', () => goToSlide(currentSlide + 1, 'next'));

  // --- Keyboard Navigation ---
  document.addEventListener('keydown', (e) => {
    // If the user has the lightbox open, keyboard should not slide pages (except ESC to close)
    if (umlLightbox.classList.contains('active')) {
      if (e.key === 'Escape') {
        closeLightbox();
      }
      return;
    }

    if (e.key === 'ArrowRight' || e.key === ' ') {
      e.preventDefault();
      goToSlide(currentSlide + 1, 'next');
    } else if (e.key === 'ArrowLeft') {
      e.preventDefault();
      goToSlide(currentSlide - 1, 'prev');
    }
  });

  // --- Sidebar Navigation Menu ---
  function openSidebar() {
    sidebarMenu.classList.add('open');
    sidebarOverlay.classList.add('active');
  }

  function closeSidebar() {
    sidebarMenu.classList.remove('open');
    sidebarOverlay.classList.remove('active');
  }

  menuToggleBtn.addEventListener('click', openSidebar);
  sidebarCloseBtn.addEventListener('click', closeSidebar);
  sidebarOverlay.addEventListener('click', closeSidebar);

  // Jump to slide from sidebar click
  sidebarItems.forEach(item => {
    item.addEventListener('click', () => {
      const targetIndex = parseInt(item.getAttribute('data-slide'), 10);
      const direction = targetIndex > currentSlide ? 'next' : 'prev';
      goToSlide(targetIndex, direction);
      closeSidebar();
    });
  });

  // --- Automatic UML Image Loading Fallback ---
  const images = document.querySelectorAll('.diagram-container img, .lightbox-image');
  images.forEach(img => {
    img.addEventListener('error', function() {
      // Check if the current source contains 'les digramme/' and try 'diagrammes/' instead
      if (this.src.includes('les%20digramme/') || this.src.includes('les digramme/')) {
        this.src = this.src.replace(/les%20digramme\/|les\ digramme\//g, 'diagrammes/');
      } 
      // Or if it failed with 'diagrammes/' and try 'les digramme/' instead
      else if (this.src.includes('diagrammes/')) {
        this.src = this.src.replace('diagrammes/', 'les digramme/');
      }
    });
  });

  // --- Interactive Lightbox Zoom & Pan ---
  const diagramContainers = document.querySelectorAll('.diagram-container');
  
  diagramContainers.forEach(container => {
    container.addEventListener('click', () => {
      const diagramType = container.getAttribute('data-diagram');
      const imgElement = container.querySelector('img');
      
      if (imgElement && imgElement.src) {
        openLightbox(imgElement.src, diagramType);
      }
    });
  });

  function openLightbox(src, type) {
    // Set Image src
    lightboxImage.src = src;
    
    // Set Caption Info
    const details = umlDetails[type] || { title: "Visualisation UML", desc: "Aperçu du diagramme de conception." };
    lightboxTitle.textContent = details.title;
    lightboxDesc.textContent = details.desc;
    
    // Reset Zoom and Position State
    zoomLevel = 1.0;
    panX = 0;
    panY = 0;
    updateImageTransform();
    
    // Open Modal
    umlLightbox.classList.add('active');
    
    // Trigger GSAP entrance animation for lightbox modal
    if (window.gsap) {
      gsap.fromTo('#umlLightbox', { opacity: 0 }, { opacity: 1, duration: 0.3 });
      gsap.fromTo('.lightbox-image', { scale: 0.8 }, { scale: 1, duration: 0.4, delay: 0.1, ease: 'back.out(1.2)' });
    }
  }

  function closeLightbox() {
    if (window.gsap) {
      gsap.to('#umlLightbox', { 
        opacity: 0, 
        duration: 0.25, 
        onComplete: () => {
          umlLightbox.classList.remove('active');
        } 
      });
    } else {
      umlLightbox.classList.remove('active');
    }
  }

  lightboxClose.addEventListener('click', closeLightbox);
  umlLightbox.addEventListener('click', (e) => {
    // Close lightbox only when clicking outside the image and control components
    if (e.target === umlLightbox || e.target === document.querySelector('.lightbox-view')) {
      closeLightbox();
    }
  });

  // Zoom Operations
  function zoom(factor) {
    zoomLevel = Math.max(minZoom, Math.min(maxZoom, zoomLevel + factor));
    updateImageTransform();
  }

  function resetZoom() {
    zoomLevel = 1.0;
    panX = 0;
    panY = 0;
    updateImageTransform();
  }

  function updateImageTransform() {
    lightboxImage.style.transform = `translate(${panX}px, ${panY}px) scale(${zoomLevel})`;
    zoomIndicator.textContent = `${Math.round(zoomLevel * 100)}%`;
  }

  zoomInBtn.addEventListener('click', () => zoom(zoomStep));
  zoomOutBtn.addEventListener('click', () => zoom(-zoomStep));
  zoomResetBtn.addEventListener('click', resetZoom);

  // Mouse wheel zoom support
  umlLightbox.addEventListener('wheel', (e) => {
    e.preventDefault();
    if (e.deltaY < 0) {
      zoom(zoomStep); // Scroll up -> zoom in
    } else {
      zoom(-zoomStep); // Scroll down -> zoom out
    }
  }, { passive: false });

  // Pan (Drag and drop) Support
  lightboxImgWrapper.addEventListener('mousedown', (e) => {
    e.preventDefault();
    isDragging = true;
    startX = e.clientX - panX;
    startY = e.clientY - panY;
    lightboxImgWrapper.style.cursor = 'grabbing';
  });

  document.addEventListener('mousemove', (e) => {
    if (!isDragging) return;
    panX = e.clientX - startX;
    panY = e.clientY - startY;
    updateImageTransform();
  });

  document.addEventListener('mouseup', () => {
    if (isDragging) {
      isDragging = false;
      lightboxImgWrapper.style.cursor = 'grab';
    }
  });

  lightboxImgWrapper.addEventListener('mouseleave', () => {
    if (isDragging) {
      isDragging = false;
      lightboxImgWrapper.style.cursor = 'grab';
    }
  });

  // Touch support for mobile slides swiping (optional, but premium)
  let touchStartX = 0;
  let touchEndX = 0;

  document.addEventListener('touchstart', (e) => {
    // Disable swiping gestures inside lightbox to allow panning
    if (umlLightbox.classList.contains('active')) return;
    touchStartX = e.changedTouches[0].screenX;
  }, { passive: true });

  document.addEventListener('touchend', (e) => {
    if (umlLightbox.classList.contains('active')) return;
    touchEndX = e.changedTouches[0].screenX;
    handleSwipe();
  }, { passive: true });

  function handleSwipe() {
    const swipeThreshold = 50; // pixels
    if (touchEndX < touchStartX - swipeThreshold) {
      goToSlide(currentSlide + 1, 'next'); // Swipe Left -> Next Slide
    }
    if (touchEndX > touchStartX + swipeThreshold) {
      goToSlide(currentSlide - 1, 'prev'); // Swipe Right -> Prev Slide
    }
  }

});

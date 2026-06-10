import React, { useState, useEffect, useRef, useContext } from "react";
import { useNavigate } from "react-router-dom";
import API from "../axios";
import AuthContext from "../Context/AuthContext";
import "./ChatBot.css";

const ChatBot = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [inputValue, setInputValue] = useState("");
  const [isSending, setIsSending] = useState(false);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [error, setError] = useState(null);

  const { isAuthenticated } = useContext(AuthContext);
  const navigate = useNavigate();
  const messagesEndRef = useRef(null);

  // Fonction pour scroller au bas de la discussion
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  // Charger l'historique quand le chat s'ouvre
  useEffect(() => {
    if (isOpen && isAuthenticated) {
      const loadHistory = async () => {
        try {
          setLoadingHistory(true);
          const response = await API.get("/chat/history");
          setMessages(response.data);
        } catch (err) {
          console.error("Erreur de chargement de l'historique :", err);
          setError("Impossible de charger votre historique.");
        } finally {
          setLoadingHistory(false);
          setTimeout(scrollToBottom, 50);
        }
      };

      loadHistory();
    }
  }, [isOpen, isAuthenticated]);

  // Scroller auto à chaque nouveau message
  useEffect(() => {
    scrollToBottom();
  }, [messages, isSending]);

  // Formater la date/heure
  const formatTime = (isoString) => {
    if (!isoString) return "";
    try {
      const date = new Date(isoString);
      return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    } catch (e) {
      return "";
    }
  };

  // Vider l'historique
  const handleClearHistory = async () => {
    if (window.confirm("Voulez-vous vraiment effacer tout votre historique avec l'assistant ?")) {
      try {
        await API.delete("/chat/history");
        setMessages([]);
      } catch (err) {
        console.error("Erreur de suppression de l'historique :", err);
        alert("Une erreur est survenue lors de la suppression.");
      }
    }
  };

  // Envoyer un message
  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!inputValue.trim() || isSending) return;

    const userText = inputValue.trim();
    setInputValue("");
    setError(null);

    // Ajouter le message utilisateur dans le state immédiatement
    const userMsg = {
      id: Date.now(),
      role: "user",
      content: userText,
      timestamp: new Date().toISOString(),
    };
    setMessages((prev) => [...prev, userMsg]);
    setIsSending(true);

    try {
      const response = await API.post("/chat", { message: userText });
      const botMsg = {
        id: Date.now() + 1,
        role: "assistant",
        content: response.data.answer,
        timestamp: new Date().toISOString(),
      };
      setMessages((prev) => [...prev, botMsg]);
    } catch (err) {
      console.error("Erreur lors de l'appel au chatbot :", err);
      const errorMsg = {
        id: Date.now() + 2,
        role: "assistant",
        content: "Désolé, je rencontre des difficultés techniques actuellement. Veuillez réessayer.",
        timestamp: new Date().toISOString(),
        isError: true,
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setIsSending(false);
    }
  };

  return (
    <>
      {/* Bouton Flottant (Lanceur) */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className={`chatbot-launcher ${isOpen ? "open" : ""}`}
        aria-label="Contacter l'assistant de discussion"
      >
        {isOpen ? (
          // Icone Fermer (X)
          <svg viewBox="0 0 24 24">
            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z" />
          </svg>
        ) : (
          // Icone Bulle Chat
          <svg viewBox="0 0 24 24">
            <path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 9h12v2H6V9zm8 5H6v-2h8v2zm4-6H6V6h12v2z" />
          </svg>
        )}
      </button>

      {/* Fenêtre de Discussion */}
      <div className={`chatbot-window ${isOpen ? "open" : ""}`}>
        {/* Header */}
        <div className="chat-header">
          <div className="chat-header-info">
            <div className="chat-avatar">DT</div>
            <div className="chat-title-container">
              <h4 className="chat-title">Assistant DigiTech</h4>
              <span className="chat-status">En ligne</span>
            </div>
          </div>
          <div className="chat-actions">
            {isAuthenticated && messages.length > 0 && (
              <button
                onClick={handleClearHistory}
                className="chat-header-btn"
                title="Effacer la discussion"
              >
                {/* Trash Icon */}
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                  <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z" />
                </svg>
              </button>
            )}
            <button
              onClick={() => setIsOpen(false)}
              className="chat-header-btn"
              title="Fermer le chat"
            >
              {/* Close Icon */}
              <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z" />
              </svg>
            </button>
          </div>
        </div>

        {/* Corps - Si l'utilisateur n'est PAS connecté */}
        {!isAuthenticated ? (
          <div className="chat-unauth-container">
            <div className="chat-unauth-icon">💬</div>
            <h5 className="chat-unauth-title">Besoin d'aide ?</h5>
            <p className="chat-unauth-desc">
              Connectez-vous à votre compte DigiTech pour discuter avec notre assistant IA intelligent.
              Il pourra vous guider pour vos achats, commandes et livraisons !
            </p>
            <button
              onClick={() => {
                setIsOpen(false);
                navigate("/login");
              }}
              className="chat-login-btn"
            >
              Se connecter
            </button>
          </div>
        ) : (
          /* Corps - Si l'utilisateur EST connecté */
          <>
            {/* Zone des messages */}
            <div className="chat-messages">
              {loadingHistory ? (
                <div style={{ textAlign: "center", padding: "20px", color: "#6b7280" }}>
                  Chargement de l'historique...
                </div>
              ) : messages.length === 0 ? (
                <div style={{ textAlign: "center", padding: "30px 20px", color: "#6b7280", fontSize: "0.85rem", lineHeight: "1.5" }}>
                  👋 Bonjour ! Je suis l'assistant virtuel de DigiTech. Comment puis-je vous aider aujourd'hui ?
                  <br />
                  <span style={{ fontSize: "0.75rem", display: "block", marginTop: "10px", color: "#9ca3af" }}>
                    Posez vos questions sur la livraison, les retours, ou nos services !
                  </span>
                </div>
              ) : (
                messages.map((msg) => (
                  <div key={msg.id} className={`chat-message-row ${msg.role}`}>
                    <div className="chat-bubble">
                      {msg.content}
                      <span className="chat-time">{formatTime(msg.timestamp)}</span>
                    </div>
                  </div>
                ))
              )}

              {/* Indicateur de saisie IA ("L'assistant écrit...") */}
              {isSending && (
                <div className="chat-typing-row">
                  <div className="chat-typing-bubble">
                    <div className="chat-typing-dots">
                      <span className="chat-typing-dot"></span>
                      <span className="chat-typing-dot"></span>
                      <span className="chat-typing-dot"></span>
                    </div>
                  </div>
                </div>
              )}
              <div ref={messagesEndRef} />
            </div>

            {/* Formulaire de saisie */}
            <form onSubmit={handleSendMessage} className="chat-input-form">
              <input
                type="text"
                value={inputValue}
                onChange={(e) => setInputValue(e.target.value)}
                placeholder="Écrivez votre message..."
                className="chat-input-field"
                disabled={isSending}
                required
              />
              <button
                type="submit"
                disabled={!inputValue.trim() || isSending}
                className="chat-send-btn"
                title="Envoyer le message"
              >
                {/* Send Icon */}
                <svg viewBox="0 0 24 24">
                  <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" />
                </svg>
              </button>
            </form>
          </>
        )}
      </div>
    </>
  );
};

export default ChatBot;

import asyncio
from fastapi import FastAPI
from pydantic import BaseModel
from telethon import TelegramClient, events
from telethon.tl.types import InputPhoneContact
from telethon.functions import contacts
from contextlib import asynccontextmanager

api_id =  
api_hash = ''  

client = TelegramClient("session_name", api_id, api_hash)

# -----------------------------
#  LIFESPAN FASTAPI
# -----------------------------
@asynccontextmanager
async def lifespan(app: FastAPI):
    print("🔌 Démarrage du client Telegram...")
    await client.start()
    print("✅ Telegram connecté.")

    yield  # ---------- API ACTIVE ----------

    print("🔌 Déconnexion Telegram...")
    await client.disconnect()


app = FastAPI(lifespan=lifespan)

# -----------------------------
#  MODELES DES REQUÊTES
# -----------------------------
class SendMessage(BaseModel):
    phone: str
    message: str

class ListenRequest(BaseModel):
    phone: str

# -----------------------------
#  SERVICE : ENVOYER MESSAGE
# -----------------------------
@app.post("/send-message")
async def send_message(data: SendMessage):

    contact = InputPhoneContact(
        client_id=hash(data.phone) % 1000000,
        phone=data.phone,
        first_name=data.phone,
        last_name=""
    )

    result = await client(contacts.ImportContactsRequest([contact]))

    if not result.users:
        return {"status": "error", "message": "Utilisateur introuvable"}

    user = result.users[0]
    await client.send_message(user, data.message)

    return {"status": "success", "message": "Message envoyé !"}

# -----------------------------
#  SERVICE : ÉCOUTER UN NUMÉRO
# -----------------------------
LISTENERS = {}

@app.post("/listen-from-number")
async def listen_from_number(data: ListenRequest):

    phone = data.phone

    # Supprimer ancien listener
    if phone in LISTENERS:
        client.remove_event_handler(LISTENERS[phone])

    async def handler(event):
        sender = await event.get_sender()
        if sender.phone == phone:
            print(f"📩 Nouveau message de {phone}: {event.message.text}")

    LISTENERS[phone] = handler
    client.add_event_handler(handler, events.NewMessage(incoming=True))

    return {"status": "listening", "phone": phone}

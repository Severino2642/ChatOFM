from telethon import TelegramClient
from telethon.errors import SessionPasswordNeededError

api_id =  
api_hash = ''
phone = ''  

client = TelegramClient("session_name", api_id, api_hash)

async def main():
    await client.start()
    if not await client.is_user_authorized():
        await client.send_code_request(phone)
        code = input("Code reçu : ")
        try:
            await client.sign_in(phone, code)
        except SessionPasswordNeededError:
            pwd = input("Mot de passe 2FA : ")
            await client.sign_in(password=pwd)
    print("Session créée avec succès.")

client.loop.run_until_complete(main())

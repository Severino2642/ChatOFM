pip install flask 
pip install telethon 

pkill -f telegram_microservice.py

pip install fastapi uvicorn telethon pydantic


uvicorn api:app --reload --port 8000

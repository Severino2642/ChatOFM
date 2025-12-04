<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Paiement Stripe</title>
    <script src="https://js.stripe.com/v3/"></script>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }

        .container {
            background: white;
            border-radius: 16px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            max-width: 500px;
            width: 100%;
            padding: 40px;
        }

        h1 {
            color: #333;
            margin-bottom: 10px;
            font-size: 28px;
        }

        .subtitle {
            color: #666;
            margin-bottom: 30px;
            font-size: 14px;
        }

        .form-group {
            margin-bottom: 25px;
        }

        label {
            display: block;
            margin-bottom: 8px;
            color: #333;
            font-weight: 600;
            font-size: 14px;
        }

        input, select {
            width: 100%;
            padding: 12px 16px;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            font-size: 16px;
            transition: border-color 0.3s;
        }

        input:focus, select:focus {
            outline: none;
            border-color: #667eea;
        }

        .currency-amount-group {
            display: grid;
            grid-template-columns: 120px 1fr;
            gap: 15px;
        }

        #payment-element {
            margin-bottom: 25px;
            padding: 20px;
            background: #f8f9fa;
            border-radius: 8px;
            min-height: 200px;
        }

        .hidden {
            display: none;
        }

        button {
            width: 100%;
            padding: 14px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.2s, box-shadow 0.2s;
        }

        button:hover:not(:disabled) {
            transform: translateY(-2px);
            box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
        }

        button:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }

        .message {
            margin-top: 20px;
            padding: 12px;
            border-radius: 8px;
            font-size: 14px;
        }

        .error {
            background: #fee;
            color: #c33;
            border: 1px solid #fcc;
        }

        .success {
            background: #efe;
            color: #3c3;
            border: 1px solid #cfc;
        }

        .loading {
            display: inline-block;
            width: 20px;
            height: 20px;
            border: 3px solid rgba(255,255,255,.3);
            border-radius: 50%;
            border-top-color: white;
            animation: spin 1s ease-in-out infinite;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }

        .amount-display {
            background: #f0f4ff;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 25px;
            text-align: center;
        }

        .amount-display .label {
            font-size: 12px;
            color: #666;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .amount-display .value {
            font-size: 32px;
            font-weight: bold;
            color: #667eea;
            margin-top: 5px;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>Paiement s&eacute;curis&eacute;</h1>
    <p class="subtitle">Entrez les d&eacute;tails de votre paiement</p>

    <form id="payment-form">
        <div class="currency-amount-group">
            <div class="form-group">
                <label for="currency">Devise</label>
                <select id="currency" required>
                    <option value="eur">EUR</option>
                    <option value="usd">USD</option>
                    <option value="gbp">GBP</option>
                    <option value="chf">CHF)</option>
                    <option value="cad">CAD</option>
                    <option value="jpy">JPY</option>
                </select>
            </div>

            <div class="form-group">
                <label for="amount">Montant</label>
                <input
                        type="number"
                        id="amount"
                        placeholder="10.00"
                        step="0.01"
                        min="0.50"
                        required>
            </div>
        </div>

        <button type="button" id="create-payment" class="hidden">
            Initialiser le paiement
        </button>
    </form>

    <div id="amount-display" class="amount-display hidden">
        <div class="label">Montant &agrave; payer</div>
        <div class="value" id="display-amount"></div>
    </div>

    <div id="payment-element" class="hidden"></div>

    <button id="submit" class="hidden" disabled>
        <span id="button-text">Payer maintenant</span>
    </button>

    <div id="message"></div>
</div>

<script>
    const stripe = Stripe('pk_test_api_key'); // Any amin'ny Tsinjo

    let elements;
    let clientSecret;

    const amountInput = document.getElementById('amount');
    const currencySelect = document.getElementById('currency');
    const createPaymentBtn = document.getElementById('create-payment');
    const paymentElement = document.getElementById('payment-element');
    const submitBtn = document.getElementById('submit');
    const messageDiv = document.getElementById('message');
    const amountDisplay = document.getElementById('amount-display');
    const displayAmount = document.getElementById('display-amount');

    amountInput.addEventListener('input', toggleCreateButton);
    currencySelect.addEventListener('change', toggleCreateButton);

    function toggleCreateButton() {
        if (amountInput.value && parseFloat(amountInput.value) >= 0.50) {
            createPaymentBtn.classList.remove('hidden');
        } else {
            createPaymentBtn.classList.add('hidden');
        }
    }

    createPaymentBtn.addEventListener('click', async () => {
        const amount = parseFloat(amountInput.value);
        const currency = currencySelect.value;

        if (amount < 0.50) {
            showMessage('Le montant minimum est 0.50', 'error');
            return;
        }

        createPaymentBtn.disabled = true;
        createPaymentBtn.innerHTML = '<span class="loading"></span> Initialisation...';

        try {
            console.log('Envoi de la requête:', {
                amount: Math.round(amount * 100),
                currency: currency
            });

            const response = await fetch('<%=request.getContextPath()%>/payment/create-payment-intent', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    amount: Math.round(amount * 100), // Convertir en centimes
                    currency: currency
                })
            });

            console.log('Status de la réponse:', response.status);

            const data = await response.json();
            console.log('Données reçues:', data);

            if (!response.ok) {
                throw new Error(data.error || 'Erreur lors de la création du paiement');
            }
            clientSecret = data.clientSecret;

            // Afficher le montant
            const currencySymbols = {
                'eur': '€', 'usd': '$', 'gbp': '£',
                'chf': 'Fr', 'cad': '$', 'jpy': '¥'
            };
            displayAmount.textContent = `${amount.toFixed(2)} ${currencySymbols[currency]}`;
            amountDisplay.classList.remove('hidden');

            // Masquer le formulaire initial
            document.getElementById('payment-form').classList.add('hidden');
            createPaymentBtn.classList.add('hidden');

            // Initialiser Stripe Elements
            elements = stripe.elements({
                clientSecret,
                appearance: {
                    theme: 'stripe',
                    variables: {
                        colorPrimary: '#667eea',
                    }
                }
            });

            const paymentElementWidget = elements.create('payment');
            paymentElementWidget.mount('#payment-element');

            paymentElement.classList.remove('hidden');
            submitBtn.classList.remove('hidden');
            submitBtn.disabled = false;

            showMessage('Vous pouvez maintenant procéder au paiement', 'success');

        } catch (error) {
            showMessage(error.message, 'error');
            createPaymentBtn.disabled = false;
            createPaymentBtn.innerHTML = 'Initialiser le paiement';
        }
    });

    // Soumettre le paiement
    submitBtn.addEventListener('click', async (e) => {
        e.preventDefault();
        submitBtn.disabled = true;
        document.getElementById('button-text').innerHTML = '<span class="loading"></span> Traitement...';

        const { error } = await stripe.confirmPayment({
            elements,
            confirmParams: {
                return_url: window.location.origin + '<%=request.getContextPath()%>/payment/success',
            }
        });

        if (error) {
            showMessage(error.message, 'error');
            submitBtn.disabled = false;
            document.getElementById('button-text').textContent = 'Payer maintenant';
        }
    });

    function showMessage(text, type = 'error') {
        messageDiv.textContent = text;
        messageDiv.className = `message ${type}`;
        setTimeout(() => {
            messageDiv.textContent = '';
            messageDiv.className = '';
        }, 5000);
    }
</script>
</body>
</html>
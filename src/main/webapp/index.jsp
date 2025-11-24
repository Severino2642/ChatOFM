<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, shrink-to-fit=no" name="viewport">
    <title>Chat OFM</title>
    <!-- General CSS Files -->
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/app.min.css">
    <!-- Template CSS -->
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/style.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/components.css">
    <!-- Custom style CSS -->
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/custom.css">
    <link rel='shortcut icon' type='image/x-icon' href='<%=request.getContextPath()%>/assets/img/favicon.ico' />
</head>
<body>
<div class="card bg-black">
    <div class="chat">
        <div class="chat-header clearfix">
            <img src="<%=request.getContextPath()%>/assets/img/users/user-6.png" alt="avatar">
            <div class="chat-about">
                <div class="chat-with">Bot</div>
            </div>
        </div>
    </div>
    <div class="chat-box" id="mychatbox">
        <div class="card-body chat-content">
        </div>
        <div class="card-footer chat-form">
            <form id="chat-form">
                <input type="text" class="form-control" id="message-input" placeholder="Type a message" autocomplete="off">
                <button class="btn btn-primary" type="submit">
                    <i class="far fa-paper-plane"></i>
                </button>
            </form>
        </div>
    </div>
</div>

<script src="<%=request.getContextPath()%>/assets/js/app.min.js"></script>
<!-- Template JS File -->
<script src="<%=request.getContextPath()%>/assets/js/scripts.js"></script>
<!-- Custom JS File -->
<script src="<%=request.getContextPath()%>/assets/js/custom.js"></script>

<script>
    "use strict";

    // Configuration
    const CHAT_CONFIG = {
        userPicture: '<%=request.getContextPath()%>/assets/img/users/user-4.png',
        botPicture: '<%=request.getContextPath()%>/assets/img/users/user-6.png',
        apiEndpoint: '<%=request.getContextPath()%>/chat' // Endpoint pour les requêtes AJAX
    };

    // Fonction de contrôle du chat
    $.chatCtrl = function (element, chat) {
        var chat = $.extend({
            position: 'chat-right',
            text: '',
            time: moment(new Date().toISOString()).format('hh:mm'),
            picture: '',
            type: 'text', // or typing
            timeout: 0,
            onShow: function () { }
        }, chat);

        var target = $(element),
            element = '<div class="chat-item ' + chat.position + '" style="display:none">' +
                '<img src="' + chat.picture + '">' +
                '<div class="chat-details">' +
                '<div class="chat-text">' + chat.text + '</div>' +
                '<div class="chat-time">' + chat.time + '</div>' +
                '</div>' +
                '</div>',
            typing_element = '<div class="chat-item chat-left chat-typing" style="display:none">' +
                '<img src="' + chat.picture + '">' +
                '<div class="chat-details">' +
                '<div class="chat-text"></div>' +
                '</div>' +
                '</div>';

        var append_element = element;
        if (chat.type == 'typing') {
            append_element = typing_element;
        }

        if (chat.timeout > 0) {
            setTimeout(function () {
                target.find('.chat-content').append($(append_element).fadeIn());
            }, chat.timeout);
        } else {
            target.find('.chat-content').append($(append_element).fadeIn());
        }

        scrollToBottom(target);
        chat.onShow.call(this, append_element);
    }

    // Fonction pour faire défiler vers le bas
    function scrollToBottom(target) {
        var target_height = 0;
        target.find('.chat-content .chat-item').each(function () {
            target_height += $(this).outerHeight();
        });
        setTimeout(function () {
            target.find('.chat-content').scrollTop(target_height, -1);
        }, 100);
    }

    // Fonction pour afficher l'indicateur de frappe
    function showTypingIndicator() {
        $.chatCtrl('#mychatbox', {
            text: '',
            picture: CHAT_CONFIG.botPicture,
            position: 'chat-left',
            type: 'typing'
        });
    }

    // Fonction pour masquer l'indicateur de frappe
    function hideTypingIndicator() {
        $('#mychatbox .chat-typing').last().remove();
    }

    // Fonction pour envoyer un message au serveur
    function sendMessageToServer(message) {
        return new Promise((resolve, reject) => {
            $.ajax({
                url: CHAT_CONFIG.apiEndpoint,
                type: 'POST',
                data: {
                    message: message,
                    timestamp: new Date().toISOString()
                },
                dataType: 'json',
                success: function(response) {
                    resolve(response);
                },
                error: function(xhr, status, error) {
                    reject(error);
                }
            });
        });
    }

    // Fonction pour traiter la réponse du bot
    function handleBotResponse(response) {
        hideTypingIndicator();

        if (response && response.reply) {
            // Afficher la réponse du bot
            $.chatCtrl('#mychatbox', {
                text: response.reply,
                picture: CHAT_CONFIG.botPicture,
                position: 'chat-left'
            });
        } else {
            // Réponse par défaut en cas d'erreur
            $.chatCtrl('#mychatbox', {
                text: 'Désolé, je n\'ai pas pu traiter votre demande.',
                picture: CHAT_CONFIG.botPicture,
                position: 'chat-left'
            });
        }
    }

    // Fonction pour gérer les erreurs
    function handleError(error) {
        hideTypingIndicator();
        console.error('Erreur:', error);

        $.chatCtrl('#mychatbox', {
            text: 'Une erreur s\'est produite. Veuillez réessayer.',
            picture: CHAT_CONFIG.botPicture,
            position: 'chat-left'
        });
    }

    // Initialisation du chat
    function initializeChat() {
        // Message de bienvenue
        $.chatCtrl('#mychatbox', {
            text: 'Bonjour ! Comment puis-je vous aider aujourd\'hui ?',
            picture: CHAT_CONFIG.botPicture,
            position: 'chat-left',
            timeout: 1000
        });

        // Configuration du défilement
        if ($("#chat-scroll").length) {
            $("#chat-scroll").css({
                height: 450
            }).niceScroll();
        }

        if ($(".chat-content").length) {
            $(".chat-content").niceScroll({
                cursoropacitymin: .3,
                cursoropacitymax: .8,
            });
            $('.chat-content').getNiceScroll(0).doScrollTop($('.chat-content').height());
        }
    }

    // Gestion de la soumission du formulaire
    $("#chat-form").submit(function (e) {
        e.preventDefault();
        var me = $(this);
        var messageInput = me.find('#message-input');
        var message = messageInput.val().trim();

        if (message.length > 0) {
            // Afficher le message de l'utilisateur
            $.chatCtrl('#mychatbox', {
                text: message,
                picture: CHAT_CONFIG.userPicture,
                position: 'chat-right'
            });

            // Réinitialiser le champ de saisie
            messageInput.val('');

            // Désactiver temporairement le formulaire
            me.find('button').prop('disabled', true);
            messageInput.prop('disabled', true);

            // Afficher l'indicateur de frappe
            showTypingIndicator();

            // Envoyer le message au serveur
            sendMessageToServer(message)
                .then(handleBotResponse)
                .catch(handleError)
                .finally(() => {
                    // Réactiver le formulaire
                    me.find('button').prop('disabled', false);
                    messageInput.prop('disabled', false);
                    messageInput.focus();
                });
        }
        return false;
    });

    // Initialisation au chargement de la page
    $(document).ready(function() {
        initializeChat();

        // Focus sur le champ de saisie
        $('#message-input').focus();

        // Gestion de la touche Entrée
        $('#message-input').keypress(function(e) {
            if (e.which == 13) {
                $('#chat-form').submit();
                return false;
            }
        });
    });
</script>

<script>
    // Debug des ressources
    window.addEventListener('load', function() {
        // Vérifier les CSS
        var sheets = document.styleSheets;
        for (var i = 0; i < sheets.length; i++) {
            try {
                var rules = sheets[i].cssRules || sheets[i].rules;
                console.log('CSS chargé:', sheets[i].href);
            } catch (e) {
                console.error('CSS non chargé:', sheets[i].href, e);
            }
        }

        // Vérifier les images
        var images = document.getElementsByTagName('img');
        for (var i = 0; i < images.length; i++) {
            images[i].onerror = function() {
                console.error('Image non chargée:', this.src);
            };
            images[i].onload = function() {
                console.log('Image chargée:', this.src);
            };
        }
    });
</script>
</body>
</html>

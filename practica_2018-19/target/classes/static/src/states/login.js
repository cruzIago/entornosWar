Spacewar.loginState = function(game) {
	this.fondo;
	this.login;
	this.avisoUso;
	this.avisoCaracteres;
	this.style;
	this.text;
	this.remove;
	this.enter;
	this.maxCaracteres;
}

Spacewar.loginState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **LOGIN** state");
		}
	},

	preload : function() {
		// In case JOIN message from server failed, we force it
		if (typeof game.global.myPlayer.id == 'undefined') {
			if (game.global.DEBUG_MODE) {
				console.log("[DEBUG] Forcing joining server...");
			}
			let message = {
				event : 'JOIN'
			}
			game.global.socket.send(JSON.stringify(message))
		}
		
		maxCaracteres = 10
		fondo = game.add.sprite(0, 0, 'fondoConLogo')
		login = game.add.sprite(420, 234, 'login')
		avisoUso = game.add.sprite(410, 430, 'avisoUso')
		avisoUso.visible = false
		avisoCaracteres = game.add.sprite(370, 480, 'avisoMaxCaracter')
		avisoCaracteres.visible = false
		
		
		style = { font: "bold 32px Arial", fill: "#fff", boundsAlignH: "left", boundsAlignV: "middle" }
	    text = game.add.text(0, 0, "Nombre", style)
	    text.setTextBounds(650, 264, 0, 0)
	},

	create : function() {			    
	    game.input.keyboard.addCallbacks(this, null, null, keyPress);
		remove = game.input.keyboard.addKey(Phaser.Keyboard.BACKSPACE);
	    remove.onDown.add(removePress, this);
	    enter = game.input.keyboard.addKey(Phaser.Keyboard.ENTER);
	    enter.onDown.add(enterPress, this);
	    game.input.keyboard.addKeyCapture([Phaser.Keyboard.ENTER, Phaser.Keyboard.BACKSPACE]);
	    
	},
	
	update: function() {
		if (game.global.response === true) {
			if (game.global.isLogin === true) {
				game.state.start('menuState')
			} else {
				avisoUso.visible = true
				game.global.response = false
			}
		}
		
		if (text.text.length > maxCaracteres-1) {
			avisoCaracteres.visible = true;
		} else {
			avisoCaracteres.visible = false;
		}
	}
}

function keyPress(char) {
	text.text += char
}

function removePress() {
	text.text = ""
}

function enterPress() {
	if (text.text.length < 10){
		let msg = new Object()
		msg.event = 'LOGIN'
		msg.text = text.text
		game.global.socket.send(JSON.stringify(msg))
	}
}
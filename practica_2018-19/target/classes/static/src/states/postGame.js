Spacewar.postGameState = function(game) {
	this.puntuacion;
	this.media;
	this.isGanador;
}

Spacewar.postGameState.prototype = {

	init : function(mensaje) {
		game.global.gameCreated = false;
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **POST GAME** state");
		}
		this.puntuacion = mensaje.puntuacionFinal;
		this.media = mensaje.media;
		this.isGanador = mensaje.isWinner;
	},

	preload : function() {

	},

	create : function() {
		bMenu = game.add.button(640, 600, 'enviar', irMenuClick, this);
		bMenu.onInputOver.add(over, {
			button : bMenu
		});
		bMenu.onInputOut.add(out, {
			button : bMenu
		});

		textoPuntuacion = game.add.text(800, 100, this.puntuacion, {
			font : "30px Arial",
			fill : "#ffffff"
		});

		textoMedia = game.add.text(800, 300, this.media, {
			font : "30px Arial",
			fill : "#ffffff"
		})

	},

	update : function() {
	}

}

function irMenuClick() {
	game.state.start("menuState");
}
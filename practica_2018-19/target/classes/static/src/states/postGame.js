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
		fondo = game.add.sprite(0, 0, 'fondoConLogo')
	},

	create : function() {
		bMenu = game.add.button(500, 350, 'menu', irMenuClick, this);
		bMenu.onInputOver.add(over, {
			button : bMenu
		});
		bMenu.onInputOut.add(out, {
			button : bMenu
		});
		
		cuadroPuntuacionMedia = game.add.sprite(800, 100, 'salonFama_Chat')
		
		textoPuntuacion = game.add.text(885, 250, this.puntuacion, {
			font : "30px Arial",
			fill : "#ffffff"
		});
		
		textoMedia = game.add.text(885, 400, "Nivel de jugador\n          "+this.media, {
			font : "30px Arial",
			fill : "#ffffff"
		})
		
		if (this.isGanador) {
			imagenFinal = game.add.sprite(75, 150, 'nave_victoria')
		} else {
			imagenFinal = game.add.sprite(75, 150, 'nave_derrota')
		}

	},

	update : function() {
	}

}

function irMenuClick() {
	game.state.start("menuState");
}
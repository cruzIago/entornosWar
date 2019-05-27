Spacewar.preloadState = function(game) {

}

Spacewar.preloadState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **PRELOAD** state");
		}
	},

	preload : function() {
		game.load.atlas('spacewar', 'assets/atlas/spacewar.png',
				'assets/atlas/spacewar.json',
				Phaser.Loader.TEXTURE_ATLAS_JSON_HASH)
		game.load.atlas('explosion', 'assets/atlas/explosion.png',
				'assets/atlas/explosion.json',
				Phaser.Loader.TEXTURE_ATLAS_JSON_HASH)
		game.load.image('avisoUso', 'assets/images/AvisoEnUso.png')
		game.load.image('avisoMaxCaracter', 'assets/images/AvisoMaxCaracteres.png')
		game.load.image('buscando', 'assets/images/buscando.png')
		game.load.image('cancelar', 'assets/images/cancelar.png')
		game.load.image('fondoConLogo', 'assets/images/fondoConLogo.png')
		game.load.image('login', 'assets/images/login.png')
		game.load.image('matchmaking', 'assets/images/matchmaking.png')
		game.load.image('crearSala', 'assets/images/crearSala.png')
		game.load.image('empezar', 'assets/images/empezar.png')
		game.load.image('salaBattleRoyal', 'assets/images/salaBattleRoyal.png')
		game.load.image('salaClassic', 'assets/images/salaClassic.png')
		game.load.image('sala', 'assets/images/sala.png')
		game.load.image('salaSeleccionada', 'assets/images/salaSeleccionada.png')
		game.load.image('salonFama_Chat', 'assets/images/salonFama_Chat.png')
		game.load.image('enviar', 'assets/images/enviar.png')
		game.load.image('cuadradoRoyale','assets/images/cuadrado_royale.png')
		game.load.image('municion','assets/images/municion.png')
	},

	create : function() {
		game.state.start('loginState')
	},

	update : function() {

	}
}
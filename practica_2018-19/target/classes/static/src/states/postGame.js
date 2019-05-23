Spacewar.postGameState = function(game) {

}

Spacewar.postGameState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **POST GAME** state");
		}
	},

	preload : function() {

	},

	create : function() {
		botonMenu=game.create.button(); //Funcion para ir al menu
		textoPuntuaciones=game.create.text();
		textoResultados=game.create.text(); //Dividir el string o que venga con \n ?
	},

	update : function() {

	}
	
}

function irMenu(){
	game.state.start("menuState");
}
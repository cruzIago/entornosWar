Spacewar.postGameState = function(game) {

}

Spacewar.postGameState.prototype = {

	init : function() {
		game.global.gameCreated=false;
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **POST GAME** state");
		}
	},

	preload : function() {

	},

	create : function() {
		bMenu = game.add.button(640, 600, 'enviar', irMenuClick, this);
		bMenu.onInputOver.add(over, {button:bMenu});
		bMenu.onInputOut.add(out, {button:bMenu});
		//textoPuntuaciones=game.create.text();
		//textoResultados=game.create.text(); //Dividir el string o que venga con \n ?*/
	},

	update : function() {
	}
	
}

function irMenuClick(){
	game.state.start("menuState");
}
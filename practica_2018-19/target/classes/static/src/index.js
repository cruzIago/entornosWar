window.onload = function() {

	game = new Phaser.Game(1280, 720, Phaser.AUTO, 'gameDiv')

	// GLOBAL VARIABLES
	game.global = {
		FPS : 30,
		DEBUG_MODE : false,
		socket : null,
		myPlayer : new Object(),
		otherPlayers : [],
		projectiles : [],
		municiones: [],
		salas : [],
		chat: [],
		puntuaciones : [],
		nombreJugador : '',
		response: false, 
		isGameStarting: false,
		gameCreated: false,
		modoJuego: ''
	}

	// WEBSOCKET CONFIGURATOR
	game.global.socket = new WebSocket("ws://127.0.0.1:8080/spacewar")
	
	game.global.socket.onopen = () => {
		if (game.global.DEBUG_MODE) {
			console.log('[DEBUG] WebSocket connection opened.')
		}
	}

	game.global.socket.onclose = () => {
		if (game.global.DEBUG_MODE) {
			console.log('[DEBUG] WebSocket connection closed.')
		}
	}
	
	game.global.socket.onmessage = (message) => {
		var msg = JSON.parse(message.data)
		
		switch (msg.event) {
		case 'MENU STATE UPDATE':
			game.global.salas = []
			game.global.chat = []
			
			for (var sala of msg.salas) {
				if(typeof sala !=='undefined'){
					let nuevaSala=new Object()
					nuevaSala.nPlayers=sala.nPlayers
					nuevaSala.nombre=sala.nombre
					nuevaSala.modoJuego=sala.modoJuego
					nuevaSala.puntuaciones="";
					nuevaSala.creador = sala.creador
					nuevaSala.inProgress = sala.inProgress
					game.global.salas.push(nuevaSala)
				}
			}
			
			
			
			for (var lineChat of msg.chat) {
				if(typeof lineChat !== 'undefined') {
					game.global.chat.push(lineChat)
				}
			}
			
			for(var puntuacion of msg.puntuaciones){
				if(typeof puntuacion.pos !=='undefined'){
					game.global.puntuaciones[puntuacion.pos].posicion.visible=true;
					
					game.global.puntuaciones[puntuacion.pos].nombre.text=puntuacion.nombreJugador;
					game.global.puntuaciones[puntuacion.pos].nombre.visible=true;
					
					game.global.puntuaciones[puntuacion.pos].media.text=puntuacion.media;
					game.global.puntuaciones[puntuacion.pos].media.visible=true;
				}
			}
			
			game.global.updateMenu();
			break
			
		case 'LOGIN':
			game.global.nombreJugador = msg.nombreJugador
			game.global.response = true;			
			break
			
		case 'JOIN':
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] JOIN message recieved')
				console.dir(msg)
			}
			game.global.myPlayer.id = msg.id
			game.global.myPlayer.shipType = msg.shipType
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] ID assigned to player: ' + game.global.myPlayer.id)
			}
			break
			
		case 'NEW ROOM' :
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] NEW ROOM message recieved')
				console.dir(msg)
			}
			
			// El jugador recibe el nombre de la sala y los limites de la misma
			// para que la cámara sepa donde seguir
			game.global.myPlayer.room = {
					name : msg.room,
					xBounds : msg.xBounds,
					yBounds: msg.yBounds
			}
			break
			
		case 'MATCHMAKING FAIL':
			game.global.cancelMatchmaking();
			break
			
		case 'CANCEL SALA BY HOST':
			game.global.targetSala(msg.indiceSala);
			break
		
		case 'MATCHMAKING SUCCESS':
			game.global.targetSala(msg.indiceSala);
			break
			
		case 'SALAS LIMIT':
			game.global.response=true;
			break
			
		case 'GAME STATE UPDATE' :
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] GAME STATE UPDATE message recieved')
				console.dir(msg)
			}
			if (typeof game.global.myPlayer.image !== 'undefined') {
				for (var player of msg.players) {
					if (game.global.myPlayer.id == player.id) {
						game.global.myPlayer.image.x = player.posX
						game.global.myPlayer.image.y = player.posY
						game.global.myPlayer.image.angle = player.facingAngle
						
						// Text y life son variables de texto encima del jugador
						game.global.myPlayer.text.x = player.posX
						game.global.myPlayer.text.y = player.posY - game.global.myPlayer.image.height
						game.global.myPlayer.life.x = player.posX
						game.global.myPlayer.life.y = player.posY - game.global.myPlayer.image.height/1.5
						game.global.myPlayer.life.text = player.vida + "%"
						game.global.myPlayer.ammo.text = "Munición: "+player.municion + "/" + game.global.myPlayer.initialAmmo
						game.global.myPlayer.fuel.text = "Propulsión: "+Math.trunc(player.fuel)+"%"
						
					} else {
						if (typeof game.global.otherPlayers[player.id] == 'undefined') {
							game.global.otherPlayers[player.id]={};
							game.global.otherPlayers[player.id].nombre=player.nombre;
							game.global.otherPlayers[player.id].shipType=player.shipType;
							game.global.otherPlayers[player.id].posX=player.posX;
							game.global.otherPlayers[player.id].posY=player.posY;
							game.global.otherPlayers[player.id].vida=player.vida;
							
						} else {
						  if(typeof game.global.otherPlayers[player.id].image !=='undefined'){
							  	game.global.otherPlayers[player.id].image.x = player.posX
							  	game.global.otherPlayers[player.id].image.y = player.posY
							  	game.global.otherPlayers[player.id].image.angle = player.facingAngle
							  	game.global.otherPlayers[player.id].text.x=player.posX
								game.global.otherPlayers[player.id].text.y=player.posY - game.global.myPlayer.image.height
								game.global.otherPlayers[player.id].vida.text=player.vida + "%"
								game.global.otherPlayers[player.id].vida.x=player.posX
								game.global.otherPlayers[player.id].vida.y=player.posY - game.global.myPlayer.image.height/1.5
							
							}else{
								game.global.otherPlayers[player.id].image=game.add.sprite(game.global.otherPlayers[player.id].posX, game.global.otherPlayers[player.id].posY,'spacewar',game.global.otherPlayers[player.id].shipType);
								game.global.otherPlayers[player.id].text=game.add.text(game.global.otherPlayers[player.id].posX,game.global.otherPlayers[player.id].posY-game.global.otherPlayers[player.id].image.height,game.global.otherPlayers[player.id].nombre,{
									font : "16px Arial",
									fill : "#ffffff"
								});
								game.global.otherPlayers[player.id].vida=game.add.text(game.global.otherPlayers[player.id].posX,game.global.otherPlayers[player.id].posY-game.global.otherPlayers[player.id].image.height/1.5,game.global.otherPlayers[player.id].vida,{
									font : "16px Arial",
									fill : "#ffffff"
								});
						
							game.global.otherPlayers[player.id].image.anchor.setTo(0.5, 0.5)
							game.global.otherPlayers[player.id].text.anchor.setTo(0.5, 0.5)
							game.global.otherPlayers[player.id].vida.anchor.setTo(0.5, 0.5)
							}
						}
					}
				}
				
				for (var projectile of msg.projectiles) {
					if (projectile.isAlive) {
						game.global.projectiles[projectile.id].image.x = projectile.posX
						game.global.projectiles[projectile.id].image.y = projectile.posY
						if (game.global.projectiles[projectile.id].image.visible === false) {
							game.global.projectiles[projectile.id].image.angle = projectile.facingAngle
							game.global.projectiles[projectile.id].image.visible = true
						}
					} else {
						if (projectile.isHit) {
							// we load explosion
							let explosion = game.add.sprite(projectile.posX, projectile.posY, 'explosion')
							explosion.animations.add('explosion')
							explosion.anchor.setTo(0.5, 0.5)
							explosion.scale.setTo(2, 2)
							explosion.animations.play('explosion', 15, false, true)
						}
						game.global.projectiles[projectile.id].image.visible = false
					}
				}
				
				// Hacer que recoja las puntuaciones y las ponga en un string
				// para visualizar tanto en partida como en postpartida
				if(typeof msg.pos_x !== 'undefined' && typeof msg.pos_y !== 'undefined' ){
					if(typeof game.global.royaleBounds === 'undefined' && game.global.modoJuego === 'Battle Royal'){
						game.global.royaleBounds=game.add.sprite(0,0,'cuadradoRoyale');
					}else{
						game.global.royaleBounds.x = msg.pos_x;
						game.global.royaleBounds.y = msg.pos_y;
						game.global.royaleBounds.width = msg.escalado_x;
						game.global.royaleBounds.height = msg.escalado_y;
					}
				}
				for(var muni of msg.municiones){
					if(muni.isAlive){
						game.global.municiones[muni.id].image.x=muni.posX;
						game.global.municiones[muni.id].image.y=muni.posY;
						if(game.global.municiones[muni.id].image.visible===false){
							game.global.municiones[muni.id].image.visible=true
						}
					}else{
						game.global.municiones[muni.id].image.visible=false;
					}
				}
				
			}
			break
			
		case 'START GAME':
			game.global.xBounds=msg.x_bounds;
			game.global.yBounds=msg.y_bounds;
			game.global.modoJuego = msg.modoJuego;
			game.global.myPlayer.initialAmmo=msg.municion;
			game.global.isGameStarting=true;
			break
			
		case 'END GAME':
			game.global.finishGame(msg); 
			game.global.otherPlayers=[]
			break
			
		case 'REMOVE PLAYER' :
			if (game.global.DEBUG_MODE) {
				console.log('[DEBUG] REMOVE PLAYER message recieved')
				console.dir(msg.players)
			}
			
			if (typeof game.global.otherPlayers[msg.id] !== 'undefined'){
				game.global.otherPlayers[msg.id].image.destroy()
				game.global.otherPlayers[msg.id].text.destroy()
				game.global.otherPlayers[msg.id].vida.destroy()
				delete game.global.otherPlayers[msg.id]
			}
		default :
			console.dir(msg)
			break
		}
	}

	// PHASER SCENE CONFIGURATOR
	game.state.add('bootState', Spacewar.bootState)
	game.state.add('preloadState', Spacewar.preloadState)
	game.state.add('loginState', Spacewar.loginState)
	game.state.add('menuState', Spacewar.menuState)
	game.state.add('lobbyState', Spacewar.lobbyState)
	game.state.add('matchmakingState', Spacewar.matchmakingState)
	game.state.add('roomState', Spacewar.roomState)
	game.state.add('gameState', Spacewar.gameState)
	game.state.add('postGameState',Spacewar.postGameState)

	game.state.start('bootState')

}
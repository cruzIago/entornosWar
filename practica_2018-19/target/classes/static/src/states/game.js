Spacewar.gameState = function(game) {
	this.numStars = 100 // Should be canvas size dependant
	this.maxProjectiles = 800 // 8 per player
	this.maxMunicion = 10
}

Spacewar.gameState.prototype = {

	init : function() {
		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Entering **GAME** state");
		}
		game.global.finishGame = function(mensaje) {
			game.state.start("postGameState",true,false,mensaje)
		}
	},

	preload : function() {

		game.world.width = game.global.xBounds;
		game.world.height = game.global.yBounds;
		// We create a procedural starfield background
		this.numStars = (game.global.xBounds / game.global.yBounds) * 500
		for (var i = 0; i < this.numStars; i++) {
			let sprite = game.add.sprite(game.world.randomX,
					game.world.randomY, 'spacewar', 'staralpha.png');
			let random = game.rnd.realInRange(0, 0.6);
			sprite.scale.setTo(random, random)
		}

		// We preload the bullets pool
		game.global.proyectiles = new Array(this.maxProjectiles)
		for (var i = 0; i < this.maxProjectiles; i++) {
			game.global.projectiles[i] = {
				image : game.add.sprite(0, 0, 'spacewar', 'projectile.png')
			}
			game.global.projectiles[i].image.anchor.setTo(0.5, 0.5)
			game.global.projectiles[i].image.visible = false
		}
		// Precargamos la municion
		game.global.municiones = new Array(this.maxMunicion)
		for (var i = 0; i < this.maxMunicion; i++) {
			game.global.municiones[i] = {
				image : game.add.sprite(0, 0, 'municion', 'municion.png')
			}
			game.global.municiones[i].image.anchor.setTo(0.5, 0.5)
			game.global.municiones[i].image.visible = false
		}

		// we load a random ship
		let random = [ 'blue', 'darkgrey', 'green', 'metalic', 'orange',
				'purple', 'red' ]
		let randomImage = random[Math.floor(Math.random() * random.length)]
				+ '_0' + (Math.floor(Math.random() * 6) + 1) + '.png'
		game.global.myPlayer.image = game.add.sprite(game.world.randomX,
				game.world.randomY, 'spacewar', game.global.myPlayer.shipType)

		// Creamos los textos de nombre y vida del jugador y los anclamos al
		// jugador
		game.global.myPlayer.text = game.add.text(0, 0,
				game.global.nombreJugador, {
					font : "16px Arial",
					fill : "#ffffff"
				});

		game.global.myPlayer.life = game.add.text(0, 0, "100%", { 
			font : "16px Arial",
			fill : "#ffffff"
		});

		game.global.myPlayer.ammo = game.add.text(100, 600, "30/30", {
			font : "30px Arial",
			fill : "#ffffff"
		});
		game.global.myPlayer.ammo.fixedToCamera = true;

		game.global.myPlayer.fuel = game.add.text(900, 600, "100%", { 
			font : "30px Arial",
			fill : "#ffffff"
		});
		game.global.myPlayer.fuel.fixedToCamera = true;

		game.global.myPlayer.life.anchor.setTo(0.5, 0.5);
		game.global.myPlayer.text.anchor.setTo(0.5, 0.5);
		game.global.myPlayer.image.anchor.setTo(0.5, 0.5)

		// Necesario para que la cámara tenga información de cuanto seguir al
		// jugador
		game.world.setBounds(0, 0, game.global.xBounds, game.global.yBounds);
	},

	create : function() {

		for (var i = 0; i < game.global.otherPlayers.length; i++) {
			if (typeof game.global.otherPlayers[i] !== 'undefined') {
				game.global.otherPlayers[i].image = game.add.sprite(
						game.global.otherPlayers[i].posX,
						game.global.otherPlayers[i].posY, 'spacewar',
						game.global.otherPlayers[i].shipType);
				game.global.otherPlayers[i].text = game.add.text(
						game.global.otherPlayers[i].posX,
						game.global.otherPlayers[i].posY
								- game.global.otherPlayers[i].image.height,
						game.global.otherPlayers[i].nombre, {
							font : "16px Arial",
							fill : "#ffffff"
						});
				game.global.otherPlayers[i].vida = game.add.text(
						game.global.otherPlayers[i].posX,
						game.global.otherPlayers[i].posY
								- game.global.otherPlayers[i].image.height
								/ 1.5, game.global.otherPlayers[i].vida, {
							font : "16px Arial",
							fill : "#ffffff"
						});

				game.global.otherPlayers[i].image.anchor.setTo(0.5, 0.5)
				game.global.otherPlayers[i].text.anchor.setTo(0.5, 0.5)
				game.global.otherPlayers[i].vida.anchor.setTo(0.5, 0.5)
			}
		}
		
		if (game.global.modoJuego !== '' & game.global.modoJuego === 'Battle Royal') {
			game.global.royaleBounds = game.add.sprite(0, 0, 'cuadradoRoyale');
		}
		
		this.wKey = game.input.keyboard.addKey(Phaser.Keyboard.W);
		this.sKey = game.input.keyboard.addKey(Phaser.Keyboard.S);
		this.aKey = game.input.keyboard.addKey(Phaser.Keyboard.A);
		this.dKey = game.input.keyboard.addKey(Phaser.Keyboard.D);
		this.spaceKey = game.input.keyboard.addKey(Phaser.Keyboard.SPACEBAR);
		// Stop the following keys from propagating up to the browser
		game.input.keyboard.addKeyCapture([ Phaser.Keyboard.W,
				Phaser.Keyboard.S, Phaser.Keyboard.A, Phaser.Keyboard.D,
				Phaser.Keyboard.SPACEBAR ]);
		game.camera.follow(game.global.myPlayer.image);
		game.global.gameCreated = true;
	},
	update : function() {
		let msg = new Object()
		msg.event = 'UPDATE MOVEMENT'

		msg.movement = {
			thrust : false,
			brake : false,
			rotLeft : false,
			rotRight : false
		}

		msg.bullet = false

		if (this.wKey.isDown)
			msg.movement.thrust = true;
		if (this.sKey.isDown)
			msg.movement.brake = true;
		if (this.aKey.isDown)
			msg.movement.rotLeft = true;
		if (this.dKey.isDown)
			msg.movement.rotRight = true;
		if (this.spaceKey.isDown) {
			let shoot = new Object()
			shoot.event = 'SHOOT';
			shoot.gameTime = game.time.now;
			game.global.socket.send(JSON.stringify(shoot));
		}

		if (game.global.DEBUG_MODE) {
			console.log("[DEBUG] Sending UPDATE MOVEMENT message to server")
		}

		game.global.socket.send(JSON.stringify(msg))
	},
	render : function() {
		if (game.global.DEBUG_MODE) {
			game.debug.cameraInfo(game.camera, 32, 32);
			game.debug.spriteCoords(game.global.myPlayer.image, 32, 500);
		}
	}
}
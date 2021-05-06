The server:

 - Start the server by doing "java ChatServer" once inside the right file location.
 - Optional parameter -csp to bind the server to a different port (i.e. "java ChatServer -csp 14005").
 - If -csp is not used the server will bind to port 14001.  
 - Server will shutdown if any client enters the "EXIT" command.
 - ChatServer\out\production\ChatServer being the correct place to be to run "java ChatServer".

The client:

 - Start the client by doing "java ChatClient" once inside the right file location.
 - Optional parameter -ccp to bind the client to a different port (i.e "java ChatClient -ccp 14005").
 - Optional parameter -cca to bind the client to a different address (i.e. "java ChatClient -cca 192.168.10.250").
 - You may pass both paramaters (i.e. "java ChatClient -cca 192.168.10.250 -ccp 14005").
 - If -ccp is not used it will bind to 14001.
 - If -cca is not used it will bind to localhost.
 - ChatClient\out\production\ChatClient being the correct place to be to run "java ChatClient". 

Basic ChatBot:

 - SimpleChatBotClient\out\production\SimpleChatBotClient and run "java SimpleChatBotClient"
 - As this is a client it also has -ccp and -cca parameter options.
 - The bot can respond to basic phrases like "Hi" and "How are you?"

Dungeons of Doom:

 - DoD Code (Networked Edition)\out\production\DoD Code and run "java GameLogic".
 - As this is a client it also has -ccp and -cca parameter options.
 - See the Dungeons of Doom readme for further advice (inside the folder named "DoD Code (Networked Edition)").
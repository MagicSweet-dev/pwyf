# pwyf - play with your friends
Simple fabric mod that allows you to play with your friends on a server without having to worry about any server setup.

## How does it work?
Really simple - it opens a [ngrok](https://ngrok.com/) tunnel when IntegratedServer starts and closes it when it stops.<br>
Your friends can join the server using the IP ngrok gives you and play.

## Links
- [Modrinth Page](https://modrinth.com/project/pwyf)

## Building
`gradlew build`. The mod will be in `build/libs`.
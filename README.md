# starcraft-AI

### Starcraft AI for teamwork using 2APL

> Below I summarize the project briefly. For a detailed explanation please read the [Project Report](docs/report.pdf)

This project is done for the course Games and Agents, together with Roemer Vlasveld and Frank van Meeuwen. The main goal of the course was to design (an aspect of) the AI in a game of choice and in this way enhance the gameplay for the human user. We have chosen to research cooperation between multiple AI players within an Real-time Strategy game. More specifically, we have used the Multi-Agent System (MAS) paradigm to steer (small groups of) units in the game Starcraft Broodwar:

![Game Setup in Starcraft](http://www.marcvanzee.nl/tmp/starcraftimg/img1.png)

In the default A.I. for this game, when two enemy teams form an alliance they do not cooperate in any way. Cooperation is limited to not attacking each other; players do not assist each other in attacking or defending. By implementing the Multi-agent approach in the form of a BDI software model, we were able to establish communication between two allied teams in the form of negotiation for future plans. To accomplish this, we implemented the [joint plans approach by Tambe](http://teamcore.usc.edu/papers/1997/jair.pdf) and extended it to fit our purpose. 

The main part of our project was to think about a reasoning cycle for the clients to determine which action they will take. For this they communicate their plans. An important aspect of teamwork is that one must achieve both the own and the team-goals. Those team-goals are pre-defined (for instance: win) of can be proposed by each individual agent. For this weve created a protocol to achieve new team goals, or: joint plans. The reasoning cycle will be described by the
different paths. The general reasoning cycle is as follows:

![Reasoning cycle of the cooperative agent](http://www.marcvanzee.nl/tmp/starcraftimg/img2.png)

Each of these steps contain again more complex decision procedures. For instance, the step ```Select most relevant plan as new plan``` consists again of the following decisison tree:

![Selecting the most relevant plan, in the case of two agents](http://www.marcvanzee.nl/tmp/starcraftimg/img3.png)

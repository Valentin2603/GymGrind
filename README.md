# 💪 GymGrind

## Arcade Bodybuilding Simulator

GymGrind — это arcade / management игра про путь бодибилдера от новичка до профессиональной сцены.

Игрок тренируется в зале, зарабатывает деньги, управляет усталостью, покупает спортивные добавки, улучшает форму персонажа и участвует в соревнованиях по бодибилдингу.

Проект создаётся как самостоятельная игра с несколькими gameplay-системами, мини-играми и progression mechanics.

---

# 🎮 Gameplay Loop

```text
Train → Recover → Work → Earn Money → Buy Supplements
→ Improve Physique → Compete On Stage → Get Judged → Repeat
```

---

# 🔥 Main Features

## 🏋️ Multiple Training Minigames

Каждый тренажёр использует собственную игровую механику:

* 🎯 Balance Bar
* ⚡ Rhythm Gameplay
* 💥 Power Meter
* ⌨️ Skill Checks
* 🏆 Posing Minigame

Мини-игры проверяют:

* реакцию,
* точность,
* чувство ритма,
* контроль,
* скорость нажатий.

---

## 🧠 Gameplay Systems

### 📈 Progression System

Игрок улучшает:

* силу,
* выносливость,
* физическую форму,
* внешний вид персонажа.

---

### 😴 Fatigue & Recovery

Тренировки увеличивают усталость.

Игроку необходимо:

* отдыхать,
* управлять нагрузкой,
* планировать цикл тренировок.

---

### 💰 Economy System

Игрок может:

* работать,
* зарабатывать деньги,
* покупать улучшения,
* инвестировать в прогресс персонажа.

---

### 💊 Supplement System

В игре реализована система:

* спортивного питания,
* временных баффов,
* усилений характеристик.

---

### 🏆 Competition System

Финальная цель — выступление на сцене бодибилдинга.

Игрок:

* проходит вступительную катсцену,
* выполняет posing-механику,
* получает оценки судей,
* участвует в соревновании.

---

### ⭐ Judge Evaluation System

Выступление оценивается по нескольким параметрам:

* Technique
* Charisma
* Stage Presence
* Total Performance

---

🏅 Achievement System

В игре реализована полноценная система достижений, которая отслеживает прогресс игрока и поощряет развитие разных стилей игры.

Достижения выдаются за:

количество тренировок,
победы на соревнованиях,
развитие характеристик,
использование игровых механик,
выполнение особых условий.

Примеры достижений
💪 First Pump — первая успешная тренировка
🔥 No Pain No Gain — серия интенсивных тренировок
🏆 Stage Monster — высокий балл на сцене
⚡ Combo Machine — большое комбо в мини-играх
💰 Self Made — накопление крупной суммы денег
🧬 Mass Monster — достижение максимальной формы

Система достижений создаёт:

дополнительную мотивацию,
replayability,
meta progression,
долгосрочные цели для игрока.

⭐ Meta Progression

Achievements работают как отдельный слой прогрессии поверх основного gameplay loop.

Игрок получает ощущение постоянного прогресса не только через характеристики персонажа, но и через:

коллекцию достижений,
выполнение челленджей,
mastery игровых механик.
---

# 🧩 Technical Features

## ⚙️ Custom Architecture

Проект построен на modular gameplay architecture.

Используются:

* reusable gameplay systems,
* state-driven architecture,
* custom interaction framework,
* independent gameplay modules.

---

## 🖼️ Custom Rendering

Большая часть интерфейсов реализована вручную через:

* Graphics2D
* BufferedImage
* JavaFX Canvas
* custom UI rendering

---

## 🧠 Gameplay Frameworks

Реализованы собственные gameplay framework-системы:

* SkillCheck Framework
* Competition Framework
* Training Framework
* Interaction System
* Save System

---

# 🗂️ Project Structure

```text
gymgrind/
│
├── game/
├── player/
├── ui/
├── rendering/
├── training/
├── competition/
├── quests/
├── achievements/
├── tutorial/
├── save/
└── assets/
```

---

# 🛠️ Technologies

* Java 21
* JavaFX
* Graphics2D
* Maven / Gradle
* Git

---

# 🎨 Screenshots

## Gym

<img width="1898" height="986" alt="image" src="https://github.com/user-attachments/assets/5d4537cc-6273-43bc-abd7-e6d3197ff109" />


## Minigames

<img width="1367" height="789" alt="image" src="https://github.com/user-attachments/assets/8b628550-92e8-4aaa-9fc8-f5fa3fdcace7" />


## Competition Stage

<img width="1317" height="752" alt="image" src="https://github.com/user-attachments/assets/fb6722d4-86eb-48b7-a18a-90221c774d64" />


## Tutorial

<img width="1550" height="924" alt="image" src="https://github.com/user-attachments/assets/ea87088b-59d1-4885-8429-8bd73a370d5d" />


---

# 🚀 Future Plans

* More training minigames
* NPC interaction system
* Advanced posing system
* Expanded competition mechanics
* Improved AI judges
* Better progression balancing
* Character customization
* More locations

---

# 👨‍💻 Team

Developed by a student team as part of a game development module project.

---

# 📌 GitHub Repository

https://github.com/Valentin2603/GymGrind

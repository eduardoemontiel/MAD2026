# MAD2026

## Workspace
Github:
  - Repository: https://github.com/eduardoemontiel/MAD2026
  - Releases: https://github.com/eduardoemontiel/MAD2026/releases

Workspace: https://upm365.sharepoint.com/:u:/s/mad2026MiEd/IQA9UV7RhJthSZ7tt2sjsPw4AX4WyNn1B7g10JZo5kTtkQA?e=45IwG5

## Description:
An app for working out in safe air locations. You can use it to plan routes or workouts out in the open and know if the air is going to be of quality or hazardous.


## Screenshots and navigation
<img width="200" height="450" alt="WhatsApp Image 2026-04-18 at 6 37 04 PM" src="https://github.com/user-attachments/assets/0672ab48-80da-4e61-9916-19ad396a6d1b" />

<img width="200" height="450" alt="WhatsApp Image 2026-04-18 at 6 37 06 PM (2)" src="https://github.com/user-attachments/assets/455cc45b-012e-4aad-ac0f-c310be94eef0" />

<img width="200" height="450" alt="WhatsApp Image 2026-04-18 at 6 37 06 PM (1)" src="https://github.com/user-attachments/assets/07ae6480-ee77-4d3d-a028-38d0db482bce" />

<img width="200" height="450" alt="WhatsApp Image 2026-04-18 at 6 37 06 PM" src="https://github.com/user-attachments/assets/5839f52b-11f7-49cb-b86b-56bd3018eecc" />

<img width="200" height="450" alt="WhatsApp Image 2026-04-18 at 6 37 05 PM (3)" src="https://github.com/user-attachments/assets/2dec64c4-af26-4666-8b79-322bc0a42f7c" />

<img width="200" height="450" alt="WhatsApp Image 2026-04-18 at 6 37 05 PM (2)" src="https://github.com/user-attachments/assets/9342b198-8d2d-4766-a716-0df5483749cb" />

<img width="200" height="450" alt="WhatsApp Image 2026-04-18 at 6 37 05 PM" src="https://github.com/user-attachments/assets/88981f3f-4b79-4ac0-a36d-ed073e12d2fa" />





## Demo Video
Short video demonstrating how the app works and all the features:
https://upm365-my.sharepoint.com/:v:/g/personal/e_montiel_rios_alumnos_upm_es/IQBxLb4IHfL_TrIWrnJCQ_a5Afw4mXjDTRe6QPeXCdZqYdg?e=7d6Squ&nav=eyJyZWZlcnJhbEluZm8iOnsicmVmZXJyYWxBcHAiOiJTdHJlYW1XZWJBcHAiLCJyZWZlcnJhbFZpZXciOiJTaGFyZURpYWxvZy1MaW5rIiwicmVmZXJyYWxBcHBQbGF0Zm9ybSI6IldlYiIsInJlZmVycmFsTW9kZSI6InZpZXcifX0%3D


## Features
**Login Screen** click "Iniciar Sesion" to type an email. If the email belongs to a registered account it will ask for the password, if not, it will ask for a name and a new password and create an account.
**Home Screen** First page that you see once you log in. Contains: user logged in at top right corner, button to enter user id and API key, logout button, location widget with toggle for activation, and weather widget with casting and air quality measurements as well as message indicating the safety.
**Map** Window where you can see your location in a blue marker, and set other markers to create a route of your choice to check the air quality for the route and pick the healthiest option.
**Route History** Table containing all the locations the app has been used. If you select a location you are brought to an information page for the location, where you can modify it, delete it, or make a report to firebase and save it in the database.


## How to Use
**Login:** Once you open the app you need to log in with an existing email or create an account. This will take you to the **Home Page** where you need to configure the user id by setting the id and an API key for the weather service. Once this is set up, you can click the toggle button on the first widget (location gps) and the app will ask for location access permition. If you give permission, the app will refresh and show the exact location in the first widget, and the weather and air quality measurements in the second widget.
On the bottom of the screen there is a menu, where you can switch to **MAP** and open the map. From here you can click a location to set a marker, and the app will create a route and show if the air quality is safe or not. You can click clear and the route will reset. 
You can also go to the history page, from where you can check out all the past locations used on the app, all clickable for more information or modification.
To leave the app, go to the home page and click "salir" at the top. this will bring you back to the home page


## Participants
List of MAD developers:
 - Eduardo Enrique Montiel Rios: e.montiel.rios@alumnos.upm.es
 - Miguel Rodríguez de la Huerga: miguel.rdelahuerga@alumnos.upm.es

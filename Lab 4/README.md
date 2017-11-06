# 📱 Shaking Shiba -- CS65 Lab3

## 💻 General Info

### 📝 Assignment Description

In this lab, we build on top of lab 2 to implement Google Map and location tracking features to make a working prototype of the game.

For the server connectivity parts, we continued using the Volley library.

### 📱 App Flow

![lab3prompt](images/lab3prompt.png)

When you try to log in, `username` and `password` are required.

When you create an account, `username`,  `full_name`, and `password` are needed, with an option to upload an image.

Creating an account automatically signs you in, taking to the main activity which is consisted of 4 tabs. The "Settings" tab and the "Play" tab are the only ones with features as of now.

After logging in, additional information from server will be put into the settings, such as how often your location is updated, how far you can see the cats etc. Once they are loaded, you will be able to change them accordingly in your local device. The changed settings are saved to the server *upon singing out*.

You can also *change your password* in the settings page after you log in. You will have to re-enter your current password for safety.

The main game lived in the Play tab. Once you click the action button, a `MapActivity` is launched where you can see markers of cat locations and your location to start moving and collecting cats.

### 🏠 Installation

  1. `git clone`
  2. open `android studio`
  3. open the project folder (`Lab 3`)
  4. press the green triangle thingy (`Run`) on the top right ▶️
  5. wait for build and play around hoping that there are no bugs 🐛
      (don't worry, we checked)

We have also included an `APK` file of the application. Download the `APK` on your device and run it to install the app.

### 🙏 Permissions

The app asks for these permissions:

1. `INTERNET`: for connecting to the server
2. `CAMERA`: for taking a profile image
3. `STORAGE`: for storing photos and data
4. `LOCATION`: for playing the location-based game

## 🎨 Design Points

Our app follows most points in the mockups shown in the App Flow section above, with a few differences that seemed reasonable to us.

### 🙋 Sign-in and Register Account Pages

#### ✋ Log In Screen

We have a "Remember me" button that allows the user's input username and password to be saved when the user logs out. This feature is enabled by default when a new account is created.

#### 🧀 Edge cases

According to our experiments and various sources such as https://perishablepress.com/stop-using-unsafe-characters-in-urls/, there are certain characters that will cause our HTTP query to go haywire because they serve as parsers.

In an attempt to prevent any confusion, we have blocked such characters from being input into the `username` and `password` fields. This applies even when you are trying to change your password

#### ✅ Username and Password Check

We have a section right next to the `username` and `password` fields that visually notifies the user if the input username is available after checking with the server, and to notify if the password has been verified with the dialog that prompts the user to confirm their password entry.

According to these checks, the save button at the bottom of the screen will be enabled/disabled, making sure that the user has put in all the necessary data for creating an account.

#### 🚗 Signing In

When the login response is returned, we save the response as a `String` and move onto the `MainActivity` and parse it later. We do this instead of parsing the data at login because different account made from different applications will have different `key` names for the JSON Object.

For example, we pass in `username`, `password`, `full_name`, and `profile` image when signing up, and expect to get these values within successful logging in. But someone else might have `fullname` or `name_full` as the key. This gets even more complex when additional information such as the settings options come into play.

You may receive a "Parsing error" message even when the `GET` request returns a valid response. We tried to catch all the "unable to parse JSON" exceptions, but please let us know if you find any.

### 🐈 Play Tab and the Game (MapActivity + SuccessActivity)

#### 🌎 Auto-Zoom in Map

Although it was given in the rubric that we should "Update and zoom to your location on the map in real time," we concluded that this greatly interfered with the playability of the game.

We start off with a zoom where you can see around `200m` around your location. This is a comfortable scale to start the game and have a rough idea of where to go next.

But when you start looking for cats, you may want to zoom in more. This is especially true when there are two or more cats in *roughly the same location*. If we try to zoom back to whatever is set as the default zoom, the player's efforts to play the game will go to waste!

Thus, we decided to follow the user and update the camera center, but **not** the zoom.  

#### 📏 Distance Calculation

The distance from a selected cat is calculated using the `Location.distanceTo` function in the `Location` class. This method returns the distance between one location to the destination location.

We noticed that the value being returned from this method tended to differ from what the server returned to us when we tried to `Pet` the selected cat. We couldn't figure out what was wrong, and tried using different methods such as `Location.distanceBetween`, but it was still a little (or sometimes quite a lot) off.

So we decided to take this case and have a crack at it. (Thank you Anja!)

Here is the location of Cookie we receive from the server:  
![cookie](images/cookieLocation.png)

Here is our location at the point of testing:  
![location](images/ourLocation.png)

We put the locations into an [online distance calculator](https://www.movable-type.co.uk/scripts/latlong.html):  
![calculation](images/test.png)

And this is what we got. It is around 12 meters off:  
![ourResult](images/calcResult.png)

This is what the server returned. It is over 1000 meters off:  
![serverResult](images/serverResult.png)

Well... We don't know what's happening in the server, but this proves that we do base our calculations off the situational locations and update it accordingly.  

#### 🎁 Success Activity  

![success](images/successActivity.png)

Upon successful petting of the cat, a screen pops up saying you have befriended a cat. There are two buttons in this screen:  

1. `AGAIN`: takes you back to the `MapActivity`, so you can go find more cats!  

2. `DONE`: takes you to `MainActivity`'s tabbed view, so you can stop playing or go to the settings tab to change your preferences or reset the list etc.  

**NOTE**: There is, in fact, another *hidden button* in this screen. What is it? Click around to find out... 😏  

### ⚙️ Settings Tab

![settings](images/settings.png)

#### 🚶 Sign Out

Signing out sends a `POST` request to the server to update any changes in the settings. This allows you to have your settings across devices, if you sign in with the same `username` and `password`.

#### ♻️ Reset Cat List

You can *reset* the cat list from within the app! Just go to the settings page and hit the reset button when you want to re-pet a cat (you might need to reset a few times since the cat might not want to pop up) or want to have a fresh start. Or maybe you're a master collector and have collected 👏all👏 👏the👏 👏cats.👏

#### ⏰ Location Update Frequency

You can change how often your location gets updated. Remember, the map will move back to your location according to this frequency, so if you feel like this feature is a little annoying, try having the location update less often.

#### 🔥 Toggle Hard Mode

Toggling this option on will make the getting the cat list request (`catlist.pl`) go out with a `mode=hard` tag. You will need to reset the list before going back to the game to make sure the list is updated.

#### 👀 Visibility Radius

Changing the settings in this menu will determine how far you can see the cats on the map. When a selected cat goes out of this visibility radius, the information panel in the game screen will be reset to the placeholder information, prompting the user to click on a marker.

## Credits 🐶 Team Shaking Shiba
![shiba](app/src/main/res/drawable/shiba.jpg)
 - Jenny Seong
 - Anja Subasic

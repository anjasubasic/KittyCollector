# 📱 Shaking Shiba -- CS65 Lab1

## 💻 General Info

### 📝 Assignment Description

In this lab, we present a single-page application with four major fields:
- character name
- full name
- password
- profile photo

The app has the ability to save the information once all the fields have been completed.
To ensure security, once the user enters a password, a check is run to see if the user can match the password exactly.

### 🐶 Team Shaking Shiba
![shiba](app/src/main/res/drawable/shiba.jpg)
 - Jenny Seong (draws random stuff like the above mascot)
 - Anja Subasic

### 🏠 Installation

  I'm sure you're all amazing developers and know what's up, but in case you forgot...

  1. `git clone`
  2. open `android studio`
  3. open the project folder
  4. press the green triangle thingy on the top right ▶️
  5. wait for build with your fingers crossed 🤞

## 🎨 Design Points

Here is the prompt given to us:

![balsamic](http://www.cs.dartmouth.edu/~sergey/cs65/lab1/UserProfile.png)

Our app follows most points in the above mock, with a few differences that seemed reasonable to us.

#### ♻️ The clear button

The mockup shows the "I already have an account" button and the "Clear" button as interchangeable, mutually exclusive entities. We thought that the two buttons can coexist happily, so we made space for both buttons. Let there be peace!

Clear goes away when there is nothing in the fields, but will pop up any time there is something that needs to be cleaned up.

#### 👩 The profile image

In the mockup, there is only a picture in the "Share your picture" section. So what do you do when you want to change your picture? Tapping on a picture can have many expected results--it could be view profile, change profile, add pictures...

We thought that it was more intuitive to have an explicit button that tells the user they can "change" their profile when they click the button.

Also, we have a super cute default image. Woof!

#### 🚗 Navigation

It seems like the mockup shown above is a part of a bigger app that has three more parts. In order to better represent that grandeur of the app, we implemented tabs, and made our lives very difficult using a *fragment* for the assignment.

But our app is now expandable and scalable, so no regrets. Just need to catch up on sleep...

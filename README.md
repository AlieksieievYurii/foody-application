# foody-application
This is my playground project where I integrate new things which I've been learning in order to get a better experience. The project consists of [the backend](https://github.com/AlieksieievYurii/foody-backend) written on Django and a native android application.
The project itself is a food order service. I fancy Glovo design therefore I use almost the same design and colours. I've sketched the design by [Figma](https://www.figma.com/). The Figma project you can find by this [link](https://www.figma.com/file/zAv3s06qS2lBhQRxe1gUFV/Foody)

# Overview
The android application consists of three modes. The first mode is for clients where they can order food. The second mode is for cooks where they can accept and handle orders. And the last mode is for administrative purposes where is implemented adding, removing and editing products.

### 1.1 Authentication & Authorization view
Authentication is done by Token-based protocol.

<img src="git-res/2.png" width=200>&nbsp;
<img src="git-res/3.png" width=200>&nbsp;

To get access to the application user must sign up and then confirm an email. If the user checked "I want to cook", it is also needed to be accepted by the administrator.

<img src="git-res/1.png" width=200>&nbsp;
<img src="git-res/4.png" width=200>&nbsp;
<img src="git-res/7.png" width=200>&nbsp;

When the user is a cook or administrator, then there is a view to select a specific role.

<img src="git-res/5.png" width=200>&nbsp;
<img src="git-res/8.png" width=200>&nbsp;

### 2 Client view
In the client view, the user can order some food. There are round buttons that represent food categories(this is not implemented in the beta version). 

<img src="git-res/6.png" width=200>&nbsp;

Once the user selected a specific category or selected "All", the view with the list of available food is opened:

<img src="git-res/11.png" width=200>&nbsp;

If the user clicks any item, the detail view will be opened:

<img src="git-res/12.png" width=200>&nbsp;
<img src="git-res/13.png" width=200>&nbsp;

In the detail screen, the user can order one or more food. Alternatively, the user can add to the cart.
Besides, on the client view the user has a drawer view with additional options:

<img src="git-res/9.png" width=200>&nbsp;

The user can to edit the current personal information:

<img src="git-res/10.png" width=200>&nbsp;

Moreover, in the client view, there is a bottom swipeable view. That view represents a history or pending items:

<img src="git-res/14.png" width=200>&nbsp;

If the user clicks the history item, he will be redirected to that product detail, however, if the user clicks 'pending' item, the current status view will be opened:

<img src="git-res/15.png" width=200>&nbsp;

### 3 Cook view
This view is similar to the client view except there is only one button. Once that button is clicked, the cook can see the list of currently opened orders. The cook can select and handle one. The most delayed orders are highlighted and at the top of the list.

<img src="git-res/16.png" width=200>&nbsp;
<img src="git-res/17.png" width=200>&nbsp;

Once the cook has accepted some order, the detailed view is opened where the cook can change the current progress:

<img src="git-res/18.png" width=200>&nbsp;
<img src="git-res/19.png" width=200>&nbsp;

### 4 Administrator view
This view represents a control panel where the administrator can do the following:
* Accept cook request
* Add/Edit/Remove category
* Add/Edit/Remove product

<img src="git-res/20.png" width=200>&nbsp;

In the "Request" view the administrator accepts the cook requests:

<img src="git-res/21.png" width=200>&nbsp;

In the "Category" view the adminstator can add new categories as well as edit/remove olds.

<img src="git-res/22.png" width=200>&nbsp;
<img src="git-res/23.png" width=200>&nbsp;

In the "Products" view the administrator can add new products as well as edit/remore olds. Moreover, searching together with filtering is implemented:

<img src="git-res/24.png" width=200>&nbsp;
<img src="git-res/25.png" width=200>&nbsp;
<img src="git-res/26.png" width=200>&nbsp;


## Change log
* 1.0.0-beta1
  * Authentication & authorization
  * Create Client view
  * Create Cook view
  * Create Administrator view
  * Add history list view
  * Implement adding/removing/editing products/categories
  * Giving feedback
  * Ordering process
  * Editing personal information
  * Implement searching on Administrator and Client views

## What I've learnt?
* 1.0.0-beta1
  * MVVM 
  * Navigation Component
  * LottieFiles Animations
  * Timber
  * Retrofit
  * Kotlin Coroutines
  * Kotlin Flow
  * New Datastore Preferences
  * Paging 3
  * Coil
  * Kotlin Delegates
  * Android testing
  * Rotating screens experience
  * Data Binding
  * Authentication & authorization process
  * Recycler View

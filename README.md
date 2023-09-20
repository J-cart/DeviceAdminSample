
<h1 align="center">Remote Locker</h1>



<p align="center">  
📱 Introducing Remote Locker: Your Ultimate Device Security Solution!  🚀

🔐 Securing What Matters Most 🔐
Remote Locker is more than just an app; it's peace of mind. If you've ever used Google's "Find My Phone" app, you'll love what Remote Locker brings to the table. Here are some of its key advantages:

🌐 Remote Locking 🌐
Lost your device? No worries! With Remote Locker, you can lock your smartphone instantly, safeguarding your personal information from prying eyes.

📢 Audible Alarm 📢
Worried about theft? Activate the audible alarm feature to deter potential thieves and increase the chances of recovering your device.

📈 Real-time Location Tracking 📈
Pinpoint the exact location of your device, making it easier to retrieve or report the theft to authorities.
</p>
</br>

- [Design](https://www.figma.com/file/w3qyP4K8jFOWDxDSH0UVW8/Phone-Lock-App?type=design&node-id=0-1&mode=design&t=6NSdAUBXsULFIbje-0)
## Demo
  
https://github.com/J-cart/DeviceAdminSample/assets/82452881/f0b2f0ff-c4e8-4d85-a9c8-9055a04cfcf0

## Download
 Coming Soon .....
<img src="/previews/preview.gif" align="right" width="320"/>

## Tech stack & Open-source libraries
- Minimum SDK level 21
- [Kotlin](https://kotlinlang.org/) based, [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) + [Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/) for asynchronous.
- Jetpack
  - Lifecycle: Observe Android lifecycles and handle UI states upon the lifecycle changes.
  - ViewModel: Manages UI-related data holder and lifecycle aware. Allows data to survive configuration changes such as screen rotations.
  - View Binding: Binds UI components in your layouts to data sources in your app using a declarative format rather than programmatically.
- Architecture
  - MVVM Architecture (View - View Binding - ViewModel - Model)
- [ksp](https://github.com/google/ksp): Kotlin Symbol Processing API.
- [Material-Components](https://github.com/material-components/material-components-android): Material design components for building ripple animation, and CardView.
- [Coil](https://github.com/coil-kt/coil): Loading images from network.
- [Firebase](https://firebase.google.com/)

## Architecture
**Remote Locker** is based on the MVVM architecture and the Repository pattern, which follows the [Google's official architecture guidance](https://developer.android.com/topic/architecture).

**Remeote Locker** was built with [Guide to app architecture](https://developer.android.com/topic/architecture), so it would be a great sample to show how the architecture works in real-world projects.


### Architecture Overview

- Each layer follows [unidirectional event/data flow](https://developer.android.com/topic/architecture/ui-layer#udf); the UI layer emits user events to the data layer, and the data layer exposes data as a stream to other layers.
- The data layer is designed to work independently from other layers and must be pure, which means it doesn't have any dependencies on the other layers.

With this loosely coupled architecture, you can increase the reusability of components and scalability of your app.


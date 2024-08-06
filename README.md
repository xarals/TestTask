# TestTask

## Libraries
To perform the test task, the following libraries were used:
- `okhttp3` for working with get and post requests
- `picasso` for asynchronous loading of images from the network
- `core-splashscreen` - Google library for implementing splash screen

## Code structure
For convenient work with information about the user, the `User` class was created in the project with the following parameters:
- `id (Long)` - user ID
- `name (String)` - user name
- `email (String)` - user email
- `phone (String)` - user phone number
- `position (String)` - user job title
- `positionId (Integer)` - job ID
- `registrationTime (Long)` - user registration time
- `photo (String)` - photo URL

The `TestAssignmentApi` class was also created to work with API HTTP requests

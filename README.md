# Flight-Advisor
Flight Advisor

Flight advisor Service is a set of APIs for primarily finding the cheapest flight from city A to city B based on price, returning all the trip information alongside the distance.

## System Functionality
- This project is developing a layered monolith **Spring Boot** based project (Latest version 2.4.0), with an in-memory database.
- This project is an OAuth2 based project, using JWT token to secure endpoints. So you need to register first to continue using the system.
- The functionality is reached based on user role, and there are three roles in the system.
    - **Admin**: user is a predefined user (*admin@traveladvisor.com/Admin1234*). 
        - Admin needs to login first through `/login` API to get their token to contact the system.
        - This user can upload airports and flight routes.
        - Admin manages cities by adding, updating, or deleting them.
        - Actually, the admin can do anything in the system.
    -  **Client**: Clients should register first before using the system through `/register` public API. 
        -  After successful registration, they can then use the public `/login` API to get a token to contact the system successfully.
        - Client can use all read API calls.
        - Clients can add, manage their comments for a city, add, update, delete their comments, and see other comments.
        - User can get the cheapest flight by calling `/cities/travel` API and provide airport codes for [from the city] and [to the city].
    -  **Public**: it is not a role, but anonymous users use APIs under public.
        - Use `/login` API to login to the system, with username and password. Then you will get a valid token.
        - Use `/register` API to register as a client to use the system functionality.
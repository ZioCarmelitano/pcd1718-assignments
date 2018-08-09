import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {LoginComponent} from './login/login.component';
import {SidebarComponent} from './sidebar/sidebar.component';
import {MessagesComponent} from './messages/messages.component';
import {SendFieldComponent} from './send-field/send-field.component';
import {EventBusService} from "./event-bus.service";
import {ChatService} from "./chat.service";
import {AppRoutingModule} from './app-routing.module';
import {ChatComponent} from './chat/chat.component';
import {RoomComponent} from "./room/room.component";

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    SidebarComponent,
    MessagesComponent,
    SendFieldComponent,
    ChatComponent,
    RoomComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule
  ],
  providers: [
    EventBusService,
    ChatService
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}

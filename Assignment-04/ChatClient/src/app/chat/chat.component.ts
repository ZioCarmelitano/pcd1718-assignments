import {Component, OnInit} from '@angular/core';
import {ChatService} from '../chat.service';
import {Room} from '../room';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit {

  public room: Room;

  constructor(service: ChatService) {
    this.room = service.room;
  }

  ngOnInit() {
  }

}

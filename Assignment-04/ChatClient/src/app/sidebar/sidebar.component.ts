import {Component, OnInit} from '@angular/core';

import {Room} from '../room';
import {User} from '../user';
import {ChatService} from '../chat.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {

  public user: User;

  public rooms: Room[] = [];

  constructor(private service: ChatService) {
  }

  ngOnInit() {
    this.user = this.service.user;

    this.service.sendRooms();

    const roomSub = this.service.onRooms().subscribe(rooms => {
      this.rooms = rooms;
      roomSub.unsubscribe();
    });

    this.service.onNewRoom().subscribe(room => this.rooms.push(room));
  }

  addRoom() {
    console.log('addRoom called');
    const name = 'Room ' + (this.rooms.length + 1);
    this.service.sendNewRoom({name});
  }

  exit() {
    this.service.sendLeaveRoom();
    this.service.sendDeleteUser();
  }

}

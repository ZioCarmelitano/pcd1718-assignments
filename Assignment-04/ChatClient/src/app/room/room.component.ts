import {Component, Input, OnInit} from '@angular/core';
import {Room} from "../room";

@Component({
  selector: 'app-room',
  templateUrl: './room.component.html',
  styleUrls: ['./room.component.css']
})
export class RoomComponent implements OnInit {

  @Input()
  public room: Room;

  constructor() {
  }

  ngOnInit() {
  }

}

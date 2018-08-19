import {Injectable} from '@angular/core';
import {EventBusService} from './event-bus.service';
import {User} from './user';

import {Observable, Subject} from 'rxjs';
import {Room} from './room';

import {filter, map} from 'rxjs/operators';
import {Message} from './message';
import {PriorityQueue} from 'typescript-collections';
import {remove} from "lodash";

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  private static EVENTBUS = '/api/eventbus';

  private static SEND_ADDRESS = 'chat.to.server';

  private static EVENT_BUS_PREFIX = 'chat.to.client';
  private static NEW_USER = ChatService.EVENT_BUS_PREFIX + '.newUser';
  private static DELETE_USER = ChatService.EVENT_BUS_PREFIX + '.deleteUser';
  private static ROOMS = ChatService.EVENT_BUS_PREFIX + '.rooms';
  private static NEW_ROOM = ChatService.EVENT_BUS_PREFIX + '.newRoom';
  private static GET_ROOM = ChatService.EVENT_BUS_PREFIX + '.getRoom';
  private static DELETE_ROOM = ChatService.EVENT_BUS_PREFIX + '.deleteRoom';
  private static JOIN_ROOM = ChatService.EVENT_BUS_PREFIX + '.joinRoom';
  private static LEAVE_ROOM = ChatService.EVENT_BUS_PREFIX + '.leaveRoom';
  private static NEW_MESSAGE = ChatService.EVENT_BUS_PREFIX + '.newMessage';
  private static ENTER_CS = ChatService.EVENT_BUS_PREFIX + '.enterCS';
  private static EXIT_CS = ChatService.EVENT_BUS_PREFIX + '.exitCS';
  private static TIMEOUT_EXPIRED = ChatService.EVENT_BUS_PREFIX + '.timeoutExpired';

  public user: User = {
    id: 0,
    name: ''
  };
  public room: Room = {
    id: 0,
    name: ''
  };

  //Map<idUser, userClock>
  private clock = new Map<number, number>();
  private globalCounter = 0;
  private userClock = 0;

  private holdBackQueue = new PriorityQueue<Message>((a, b) => a.globalCounter - b.globalCounter);
  private pendingQueue: Message[] = [];

  private newUser: Subject<User>;
  private deleteUser: Subject<number>;
  private rooms: Subject<Room[]>;
  private newRoom: Subject<Room>;
  private getRoom: Subject<Room>;
  private deleteRoom: Subject<any>;
  private joinRoom: Subject<any>;
  private leaveRoom: Subject<any>;
  private newMessage: Subject<any>;
  private enterCS: Subject<any>;
  private exitCS: Subject<any>;
  private timeoutExpired: Subject<Room>;

  private static resetUser(user: User) {
    user.id = 0;
    user.name = '';
  }

  private static resetRoom(room: Room) {
    room.id = 0;
    room.name = '';
  }

  constructor(private eventBus: EventBusService) {
    eventBus.connect(ChatService.EVENTBUS);

    this.newUser = new Subject<User>();
    this.deleteUser = new Subject<number>();
    this.rooms = new Subject<Room[]>();
    this.newRoom = new Subject<Room>();
    this.getRoom = new Subject<Room>();
    this.deleteRoom = new Subject<any>();
    this.joinRoom = new Subject<any>();
    this.leaveRoom = new Subject<any>();
    this.newMessage = new Subject<any>();
    this.enterCS = new Subject<any>();
    this.exitCS = new Subject<any>();
    this.timeoutExpired = new Subject<Room>();

    eventBus.registerHandler(ChatService.NEW_USER, (err, msg) => {
      this.newUser.next(msg.body);
    });

    eventBus.registerHandler(ChatService.DELETE_USER, (err, msg) => {
      this.deleteUser.next(msg.body);
    });

    eventBus.registerHandler(ChatService.ROOMS, (err, msg) => {
      this.rooms.next(msg.body);
    });

    eventBus.registerHandler(ChatService.NEW_ROOM, (err, msg) => {
      this.newRoom.next(msg.body);
    });

    eventBus.registerHandler(ChatService.GET_ROOM, (err, msg) => {
      this.getRoom.next(msg.body);
    });

    eventBus.registerHandler(ChatService.DELETE_ROOM, (err, msg) => {
      this.deleteRoom.next(msg.body);
    });

    eventBus.registerHandler(ChatService.JOIN_ROOM, (err, msg) => {
      let message = msg.body;
      console.log("chatService JOIN_ROOM: ");
      console.log(message);
      if (this.room.id === message.roomId) {
        if (this.user.id === message.user.id) {
          this.userClock = 0;
          this.globalCounter = message.globalCounter;
          message.usersClock.forEach(json => this.clock.set(json.user.id, json.userClock));
        } else {
          this.clock.set(message.user.id, 0);
        }
        console.log("vettore dei clock");
        console.log(this.clock);
      }
      this.joinRoom.next(message);
    });

    eventBus.registerHandler(ChatService.LEAVE_ROOM, (err, msg) => {
      let message = msg.body;
      if (this.room.id === message.roomId) {
        if (this.user.id === message.user.id) {
          this.userClock = 0;
          this.globalCounter = 0;
        }
        this.clock.delete(message.user.id);
      }
      this.leaveRoom.next(message);
    });

    eventBus.registerHandler(ChatService.NEW_MESSAGE, (err, msg) => {
      let message = msg.body;
      if (message.globalCounter) {
        if (message.globalCounter == this.globalCounter + 1) {
          console.log("Received total order " + message);
          this.globalCounter++;
          this.causalMessageOrdering(message);
          let deliverableMessage = this.holdBackQueue.peek();
          while (deliverableMessage && deliverableMessage.globalCounter === this.globalCounter + 1) {
              this.globalCounter++;
              this.causalMessageOrdering(deliverableMessage);
              this.holdBackQueue.dequeue();
              deliverableMessage = this.holdBackQueue.peek();
          }
        }else {
          console.log("In else total order: " + message);
          this.holdBackQueue.enqueue(message);
        }
      } else {
        console.log("Error");
        if (this.room.id === message.room.id && this.user.id === message.user.id) {
            this.userClock--;
        }
      }
      //this.newMessage.next(msg.body);
    });

    eventBus.registerHandler(ChatService.ENTER_CS, (err, msg) => {
      this.enterCS.next(msg.body);
    });

    eventBus.registerHandler(ChatService.EXIT_CS, (err, msg) => {
      this.exitCS.next(msg.body);
    });

    eventBus.registerHandler(ChatService.TIMEOUT_EXPIRED, (err, msg) => {
      this.timeoutExpired.next(msg.body);
    });
  }

  sendNewUser(user: User) {
    this.eventBus.send(ChatService.SEND_ADDRESS, {
      type: 'newUser',
      request: user
    });
  }

  sendDeleteUser() {
    this.eventBus.send(ChatService.SEND_ADDRESS, {
      type: 'deleteUser',
      request: {
        userId: this.user.id
      }
    });
    ChatService.resetUser(this.user);
  }

  sendRooms() {
    this.eventBus.send(ChatService.SEND_ADDRESS, {
      type: 'rooms'
    });
  }

  sendNewRoom(room: Room) {
    this.eventBus.send(ChatService.SEND_ADDRESS, {
      type: 'newRoom',
      request: room
    });
  }

  sendGetRoom(roomId: number) {
    this.eventBus.send(ChatService.SEND_ADDRESS, {
      type: 'newRoom',
      request: {
        roomId
      }
    });
  }

  sendDeleteRoom(room: Room) {
    this.eventBus.send(ChatService.SEND_ADDRESS, {
      type: 'deleteRoom',
      request: {
        roomId: room.id
      }
    });
  }

  sendJoinRoom() {
    this.eventBus.send(ChatService.SEND_ADDRESS, {
      type: 'joinRoom',
      request: {
        roomId: this.room.id,
        user: this.user
      }
    });
  }

  sendLeaveRoom() {
    if (this.room) {
      this.eventBus.send(ChatService.SEND_ADDRESS, {
        type: 'leaveRoom',
        request: {
          roomId: this.room.id,
          user: this.user
        }
      });
    }
    ChatService.resetRoom(this.room);
  }

  sendNewMessage(content: string) {
    if (this.room.id > 0) {
      this.userClock++;
      const userClock = this.userClock;
      //setTimeout(() => {
        this.eventBus.send(ChatService.SEND_ADDRESS, {
          type: 'newMessage',
          request: {
            room: this.room,
            user: this.user,
            content,
            userClock
          }
        });
      //}, (1 + Math.random() * (5 - 1)) * 1000);
    }
  }

  sendEnterCS() {
    this.eventBus.send(ChatService.SEND_ADDRESS, {
      type: 'enterCS',
      request: {
        roomId: this.room.id,
        user: this.user
      }
    });
  }

  sendExitCS() {
    this.eventBus.send(ChatService.SEND_ADDRESS, {
      type: 'exitCS',
      request: {
        roomId: this.room.id,
        user: this.user
      }
    });
  }

  onNewUser(): Observable<User> {
    return this.newUser.asObservable();
  }

  onDeleteUser(): Observable<number> {
    return this.deleteUser.asObservable();
  }

  onRooms(): Observable<Room[]> {
    return this.rooms.asObservable();
  }

  onNewRoom(): Observable<Room> {
    return this.newRoom.asObservable();
  }

  onGetRoom(): Observable<Room> {
    return this.getRoom.asObservable();
  }

  onDeleteRoom(): Observable<number> {
    return this.deleteRoom.asObservable().pipe(map(response => response.roomId));
  }

  onJoinRoom(): Observable<any> {
    return this.joinRoom.asObservable();
  }

  onLeaveRoom(): Observable<any> {
    return this.leaveRoom.asObservable();
  }

  onNewMessage(): Observable<any> {
    return this.newMessage
      .asObservable()
      .pipe(filter(message => this.room && message.room.id === this.room.id));
  }

  onEnterCS(): Observable<any> {
    return this.enterCS.asObservable();
  }

  onExitCS(): Observable<any> {
    return this.exitCS.asObservable();
  }

  onTimeoutExpired(): Observable<Room> {
    return this.timeoutExpired.asObservable();
  }

  causalMessageOrdering(message: Message) {
    console.log(message.user.name);
    console.log("message clock: " + message.userClock);
    console.log("vector clock: " + this.clock.get(message.user.id));
    if (message.userClock === (this.clock.get(message.user.id) + 1)) {
      console.log("Received causal order " + message);
      this.newMessage.next(message);
      this.clock.set(message.user.id, message.userClock);
      let deliverableMessage = this.pendingQueue.find(x => x.userClock === (this.clock.get(x.user.id) + 1));
      while (deliverableMessage) {
        this.newMessage.next(deliverableMessage);
        this.clock.set(deliverableMessage.user.id, deliverableMessage.userClock);
        remove(this.pendingQueue, message => message.globalCounter === deliverableMessage.globalCounter);
        deliverableMessage = this.pendingQueue.find(x => x.userClock === (this.clock.get(x.user.id) + 1));
      }
    } else {
      console.log("In else causal order: ");
      console.log(message);
      this.pendingQueue.push(message);
    }

  }

}

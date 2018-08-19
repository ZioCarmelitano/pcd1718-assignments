import {User} from './user';
import {Room} from './room';

export interface Message {
  room: Room;
  user: User;
  content: string;
  userClock: number;
  globalCounter: number;
}

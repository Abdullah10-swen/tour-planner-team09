export interface Tour {
  id?: number;
  name: string;
  description: string;
  fromLocation: string;
  toLocation: string;
  transportType: string;
  distance: number;
  estimatedTime: number;
  imageUrl: string | null;
  routeInfo: string | null;
  /** Number of tour logs – computed by backend. */
  popularity?: number;
  /** Child-friendliness score 0.0–1.0 – computed by backend. */
  childFriendliness?: number;
}

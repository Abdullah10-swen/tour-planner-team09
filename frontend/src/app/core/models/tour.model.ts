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
}

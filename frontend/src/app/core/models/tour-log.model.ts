export interface TourLog {
  id?: number;
  tourId?: number;
  dateTime: string | null;
  comment: string;
  difficulty: number;
  totalDistance: number;
  totalTime: number;
  rating: number;
}

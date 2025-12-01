
import { Station, TransportType, User } from '../types';

// Safe Land Zones (Center points of districts to ensure no sea placement)
// Lat, Lng
const SAFE_ZONES = [
  { name: 'Beşiktaş Çarşı', lat: 41.0428, lng: 29.0075 },
  { name: 'Kadıköy Boğa', lat: 40.9905, lng: 29.0292 },
  { name: 'Sultanahmet', lat: 41.0054, lng: 28.9768 },
  { name: 'Şişli Camii', lat: 41.0632, lng: 28.9931 },
  { name: 'Levent Metro', lat: 41.0766, lng: 29.0135 },
  { name: 'Üsküdar Meydan', lat: 41.0268, lng: 29.0160 },
  { name: 'Fatih Camii', lat: 41.0195, lng: 28.9497 },
  { name: 'Bakırköy Özgürlük', lat: 40.9801, lng: 28.8724 },
  { name: 'Maslak İTÜ', lat: 41.1070, lng: 29.0250 },
  { name: 'Moda Sahil', lat: 40.9830, lng: 29.0270 }
];

const NAMES = {
  bus: ['Merkez Durağı', 'Meydan Hattı', 'Okul Yolu', 'Hastane Önü'],
  metro: ['M2 Çıkışı', 'M4 Girişi', 'M1 Bağlantı', 'M7 İstasyonu'],
  bike: ['Park Noktası', 'Kampüs İstasyonu', 'AVM Yanı', 'Sahil Yolu'],
  scooter: ['Köşe Başı', 'Durak Yanı', 'Kütüphane Önü', 'Spor Salonu']
};

const getRandomInt = (min: number, max: number) => Math.floor(Math.random() * (max - min + 1)) + min;

// Local storage keys
const STORAGE_KEY_STATIONS = 'urbanmove_stations';

export const fetchNearbyStations = async (): Promise<Station[]> => {
  // Simulate network delay
  await new Promise(resolve => setTimeout(resolve, 600));

  // Check if we have stations in local storage
  const stored = localStorage.getItem(STORAGE_KEY_STATIONS);
  if (stored) {
    return JSON.parse(stored);
  }

  // Generate initial mock data
  const stations: Station[] = [];
  const types: TransportType[] = ['bus', 'metro', 'bike', 'scooter'];

  // Generate exactly 10 stations on safe land zones
  for (let i = 0; i < 10; i++) {
    const zone = SAFE_ZONES[i % SAFE_ZONES.length];
    const type = types[Math.floor(Math.random() * types.length)];
    const nameList = NAMES[type];
    const baseName = nameList[Math.floor(Math.random() * nameList.length)];
    
    // Add very slight random jitter (approx 50-100m) so they aren't stacked exactly on the center
    // 0.001 degrees is roughly 111 meters
    const jitterLat = (Math.random() - 0.5) * 0.002;
    const jitterLng = (Math.random() - 0.5) * 0.002;

    const capacity = type === 'bus' || type === 'metro' ? 100 : getRandomInt(5, 15);
    const available = getRandomInt(0, capacity);
    
    let status: Station['status'] = 'active';
    if (available === 0) status = 'empty';
    if (available === capacity && (type === 'bike' || type === 'scooter')) status = 'full';
    
    stations.push({
      id: `st-${i}`,
      name: `${zone.name} - ${baseName}`,
      type,
      location: {
        lat: zone.lat + jitterLat,
        lng: zone.lng + jitterLng
      },
      capacity,
      available,
      status,
      lastUpdate: new Date().toISOString()
    });
  }

  // Save initial set to storage
  localStorage.setItem(STORAGE_KEY_STATIONS, JSON.stringify(stations));
  return stations;
};

// --- AUTH MOCK SERVICES ---

export const loginUser = async (email: string): Promise<User> => {
    await new Promise(r => setTimeout(r, 500));
    // Simple admin check: if email is admin@urbanmove.com, give admin role
    const isAdmin = email.toLowerCase() === 'admin@urbanmove.com';
    return { 
        id: `user-${email.split('@')[0]}-${Date.now()}`,
        email, 
        username: email.split('@')[0],
        role: isAdmin ? 'admin' : 'user'
    };
};

export const registerUser = async (email: string): Promise<User> => {
    await new Promise(r => setTimeout(r, 500));
    const isAdmin = email.toLowerCase() === 'admin@urbanmove.com';
    return { 
        id: `user-${email.split('@')[0]}-${Date.now()}`,
        email, 
        username: email.split('@')[0],
        role: isAdmin ? 'admin' : 'user'
    };
};

// --- CRUD SERVICES ---

export const addStationService = (station: Station, currentStations: Station[]): Station[] => {
    const updated = [...currentStations, station];
    localStorage.setItem(STORAGE_KEY_STATIONS, JSON.stringify(updated));
    return updated;
};

export const removeStationService = (id: string, currentStations: Station[]): Station[] => {
    const updated = currentStations.filter(s => s.id !== id);
    localStorage.setItem(STORAGE_KEY_STATIONS, JSON.stringify(updated));
    return updated;
};

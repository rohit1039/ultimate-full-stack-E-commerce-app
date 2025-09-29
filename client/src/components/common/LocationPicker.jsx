import { MapContainer, TileLayer, Marker, useMap } from "react-leaflet";
import L from 'leaflet';
import markerIconPng from 'leaflet/dist/images/marker-icon.png';
import markerShadowPng from 'leaflet/dist/images/marker-shadow.png';
import "leaflet/dist/leaflet.css";
import { useEffect } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faLocation, faPlusCircle } from "@fortawesome/free-solid-svg-icons";

const LocationPicker = ({ currentLocation }) => {
  L.Marker.prototype.options.icon = L.icon({
    iconUrl: markerIconPng,
    shadowUrl: markerShadowPng,
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  });

  const RecenterMap = ({ lat, lng }) => {

    const map = useMap();

    map.setView([lat, lng], map.getZoom());

    useEffect(() => {
      if (lat && lng) map.setView([lat, lng], map.getZoom());
    }, [lat, lng, map]);

    return null;
  };

  return (
    <div className="current-location-map">
      <MapContainer center={currentLocation} zoom={10} style={{ height: "350px", width: "100%" }}>
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <Marker position={currentLocation} />
        <RecenterMap lat={currentLocation.lat} lng={currentLocation.lng} />
      </MapContainer>
      <button className="cur-loc-btn"><FontAwesomeIcon className="location-icon" icon={faLocation} />Use Current Location</button>
    </div>
  );
};

export default LocationPicker;

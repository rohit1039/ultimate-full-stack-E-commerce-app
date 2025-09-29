import { faBell } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

const Badge = ({icon}) => {
    return (
        <div className="notification-container" >
            <button className="notification-icon">
                <span className="badge">5</span>
                <FontAwesomeIcon className='icon' icon={icon} size='lg' />
            </button>
        </div >
    )
}

export default Badge;
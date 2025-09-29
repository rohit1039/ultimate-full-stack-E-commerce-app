import Shipped from '../../assets/services/shipped.png';
import Prices from '../../assets/services/discount.png';
import CustomerSupport from '../../assets/services/customer-support.png';
import Exchange from '../../assets/services/exchange.png';

const Shipping = () => {
    return <div style={{ display: 'flex', justifyContent: 'center', gap: '2.85rem', alignItems: 'center', marginTop: '1.75rem' }}>

            {/* Free Shipping Section */}
            <div className='service-card'>
                <div style={{ marginTop: '1.25rem' }}>
                    <img
                        src={Shipped}
                        width="100vmax"
                        style={{
                            filter: 'invert(38%) sepia(85%) saturate(2556%) hue-rotate(181deg) brightness(94%) contrast(101%)',
                            borderRadius: '8px',
                            transition: 'transform 0.3s ease'
                        }}
                        alt="shipping"
                    />
                    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.75rem' }}>
                        <p style={{ fontWeight: 'bold', marginTop: '1rem' }}>Free Shipping</p>
                        <p className="secondary-text">Minimum order of ₹999</p>
                    </div>
                </div>
            </div>

            {/* Prices Section */}
            <div className='service-card'>
                <div style={{ marginTop: '1.25rem' }}>
                    <img
                        src={Prices}
                        width="100vmax"
                        style={{
                            filter: 'invert(38%) sepia(85%) saturate(2556%) hue-rotate(181deg) brightness(94%) contrast(101%)',
                            borderRadius: '8px',
                            transition: 'transform 0.3s ease'
                        }}
                        alt="prices"
                    />
                    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.75rem' }}>
                        <p style={{ fontWeight: 'bold', marginTop: '1rem' }}>Best Prices & Offers</p>
                        <p className="secondary-text">Upto 70% off</p>
                    </div>
                </div>
            </div>

            {/* Customer Support Section */}
            <div className='service-card'>
                <div style={{ marginTop: '1.25rem' }}>
                    <img
                        src={CustomerSupport}
                        width="100vmax"
                        style={{
                            filter: 'invert(38%) sepia(85%) saturate(2556%) hue-rotate(181deg) brightness(94%) contrast(101%)',
                            borderRadius: '8px',
                            transition: 'transform 0.3s ease'
                        }}
                        alt="customer support"
                    />
                    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.75rem' }}>
                        <p style={{ fontWeight: 'bold', marginTop: '1rem' }}>Customer Support</p>
                        <p className="secondary-text">Available 24/7</p>
                    </div>
                </div>
            </div>

            {/* Exchange Section */}
            <div className='service-card'>
                <div style={{ marginTop: '1.25rem' }}>
                    <img
                        src={Exchange}
                        width="100vmax"
                        style={{
                            filter: 'invert(38%) sepia(85%) saturate(2556%) hue-rotate(181deg) brightness(94%) contrast(101%)',
                            borderRadius: '8px',
                            transition: 'transform 0.3s ease'
                        }}
                        alt="exchange"
                    />
                    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.75rem' }}>
                        <p style={{ fontWeight: 'bold', marginTop: '1rem' }}>Easy Returns</p>
                        <p className="secondary-text">With Guaranteed refunds</p>
                    </div>
                </div>
            </div>
        </div>
}
export default Shipping;

